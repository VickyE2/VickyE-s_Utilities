/* Licensed under Apache-2.0 2024. */
package org.vicky.platform.guiscreens

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.vicky.getNextOrFirst
import org.vicky.music.MusicRegistry.genreColors
import org.vicky.platform.PlatformPlugin
import org.vicky.platform.entity.rl
import org.vicky.platform.player.PlatformPlayer
import org.vicky.platform.utils.ResourceLocation
import org.vicky.utilities.DatabaseManager.dao_s.MusicPlayerDAO
import org.vicky.utilities.Registry
import java.util.*

sealed class GuiType {
    sealed class Menu : GuiType() {
        abstract val id: ResourceLocation

        data class Chest(val rows: Int) : Menu() {
            init {
                require(rows in 1..6) { "Chest rows must be between 1 and 6" }
            }

            override val id: ResourceLocation
                get() = rl("minecraft:generic_9x$rows")
        }
        data class Generic(override val id: ResourceLocation) : Menu()
    }

    data class Container(val type: InbuiltContainers) : Menu() {
        override val id: ResourceLocation = type.id
    }
    data class Workstation(val type: InbuiltWorkstations) : Menu() {
        override val id: ResourceLocation = type.id
    }

    sealed class Entity : GuiType() {
        data class Villager(val villagerId: UUID) : Entity()
        data class Horse(val entityId: UUID) : Entity()
        data class Generic(val type: String) : Entity() // fallback
    }

    object Book : GuiType()
    object SignEditor : GuiType()

    data class Custom(val id: ResourceLocation) : GuiType()
}
enum class InbuiltContainers(val id: ResourceLocation) {
    HOPPER(rl("minecraft:hopper")),
    DISPENSER(rl("minecraft:dispenser")),
    SHULKER_BOX(rl("minecraft:shulker_box"));
}
enum class InbuiltWorkstations(val id: ResourceLocation) {
    ANVIL(rl("minecraft:anvil")),
    CRAFTING(rl("minecraft:crafting")),
    SMITHING(rl("minecraft:smithing")),
    STONECUTTER(rl("minecraft:stonecutter")),
    LOOM(rl("minecraft:loom")),
    CARTOGRAPHY_TABLE(rl("minecraft:cartography_table")),
    GRINDSTONE(rl("minecraft:grindstone")),

    FURNACE(rl("minecraft:furnace")),
    BLAST_FURNACE(rl("minecraft:blast_furnace")),
    SMOKER(rl("minecraft:smoker")),
    BREWING_STAND(rl("minecraft:brewing_stand")),

    BEACON(rl("minecraft:beacon")),
    ENCHANTMENT(rl("minecraft:enchantment"));
}

val defaultEvents = mutableSetOf(
    rl("gui:open"),
    rl("gui:close"),
    rl("gui:click"),
    rl("gui:dragged"),
    rl("gui:change_slot")
)

enum class CloseReason {
    PLAYER_CLOSED,
    REPLACED,
    DISCONNECTED,
    PROGRAMMATIC
}
enum class ClickButton {
    LEFT,
    RIGHT,
    MIDDLE,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    NUMBER_KEY,
    DROP,
    DOUBLE_CLICK,
    UNKNOWN
}

sealed class GuiLayout {

    data class Generic(
        val rows: Int
    ) : GuiLayout()

    /**
     * A layout similar to that of a chest gui using a specified number of [rows]
     * that cannot exceed 6 to determine how many total rows will be in the gui with the
     * [contentSlots] determining where actual mutable data will be in.
     */
    data class Paged(
        val rows: Int,

        val contentSlots: List<Int>,



        val nextButton: GuiButtonDefinition,
        val previousButton: GuiButtonDefinition
    ) : GuiLayout()

    data class InputRequest(
        val inputSlots: List<Int>,
        val confirmButton: GuiButtonDefinition?,
        val cancelButton: GuiButtonDefinition?
    ) : GuiLayout()

    data class AnvilInput(
        val leftInput: Int = 0,
        val rightInput: Int = 1,
        val output: Int = 2
    ) : GuiLayout()

    data class Smithing(
        val templateSlot: Int,
        val baseSlot: Int,
        val additionSlot: Int,
        val outputSlot: Int
    ) : GuiLayout()
}
sealed class GuiEvent(
    val id: ResourceLocation,
    open val gui: GuiInstance
)
{
    abstract class CancellableGuiEvent(
        id: ResourceLocation,
        gui: GuiInstance
    ) : GuiEvent(id, gui)
    {

        private var cancelled = false

        fun cancel() {
            cancelled = true
        }

        fun isCancelled(): Boolean {
            return cancelled
        }
    }

    abstract class DeferredGuiEvent(
        id: ResourceLocation,
        gui: GuiInstance
    ) : CancellableGuiEvent(id, gui)
    {

        val requestId: UUID = UUID.randomUUID()

        private var deferred = false

        fun defer() {
            deferred = true
        }

        fun isDeferred(): Boolean = deferred
    }

    data class Opened(
        override val gui: GuiInstance
    ) : GuiEvent(rl("gui:open"), gui)

    data class Closed(
        override val gui: GuiInstance,
        val reason: CloseReason
    ) : GuiEvent(rl("gui:close"), gui)

    data class ButtonClicked(
        override val gui: GuiInstance,
        val button: GuiButton,
        val slot: Int
    ) : GuiEvent(rl("gui:button_clicked"), gui)

    data class Clicked(
        override val gui: GuiInstance,
        val slot: Int?,
        val buttonId: String?,
        val button: ClickButton,
        val shift: Boolean,
        val cursor: GuiContent?,
        val current: GuiContent?,
        val selectionData: Map<String, Any> = mapOf()
    ) : CancellableGuiEvent(rl("gui:click"), gui)

    data class Dragged(
        override val gui: GuiInstance,
        val slots: Set<Int>
    ) : CancellableGuiEvent(rl("gui:dragged"), gui)

    data class SlotChanged(
        override val gui: GuiInstance,
        val slot: Int,
        val oldContent: GuiContent?,
        val newContent: GuiContent?
    ) : CancellableGuiEvent(rl("gui:change_slot"), gui)

    data class Custom(
        override val gui: GuiInstance,
        val name: String,
        val data: Map<String, Any?> = emptyMap()
    ) : CancellableGuiEvent(rl("gui:$name"), gui)

    data class DataRequested(
        override val gui: GuiInstance,
        val reason: Reason,
        val page: Int? = null,
        val query: String? = null,
        val sorting: String? = null,
        val filters: Map<String, Any?> = emptyMap(),
        val payload: Map<String, Any?> = emptyMap()
    ) : DeferredGuiEvent(rl("gui:data_requested"), gui)
    {
        enum class Reason {
            INITIAL_LOAD,
            PAGE_CHANGE,
            SORT_CHANGE,
            FILTER_CHANGE,
            SEARCH,
            REFRESH,
            MANUAL
        }
    }
}
sealed class GuiButtonAction {

    data class RequestData(
        val reason: GuiEvent.DataRequested.Reason,
        val pageDelta: Int = 0,
        val sorting: String? = null,
        val query: String? = null,
        val filters: Map<String, Any?> = emptyMap(),
        val payload: Map<String, Any?> = emptyMap()
    ) : GuiButtonAction()

    data class EmitEvent(
        val eventName: ResourceLocation,
        val data: Map<String, Any?> = emptyMap()
    ) : GuiButtonAction()

    object Close : GuiButtonAction()

    object None : GuiButtonAction()
}
data class GuiElementState(
    val hidden: Boolean = false,
    val loading: Boolean = false,
    val disabled: Boolean = false,
    val selected: Boolean = false,
    val active: Boolean = true,
    val metadata: Map<String, Any?> = emptyMap()
)

enum class Alignment {
    CENTER, CENTER_LEFT, CENTER_RIGHT,
    TOP, TOP_LEFT, TOP_RIGHT,
    BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT
}
enum class GuiIcon {
    NEXT,
    PREVIOUS,
    SEARCH,
    SORT,
    FILTER,
    CLOSE,
    CONFIRM,
    CANCEL,
    PLAY,
    PAUSE,
    STOP,
    SETTINGS,
    INFO,
    WARNING,
    DELETE,
    ADD,
    REMOVE,
    EDIT,
    REFRESH,
    DOWNLOAD,
    UPLOAD,
    MUSIC,
    PLAYER,
    INVENTORY,
    QUEST,
    SKILL,
    SHOP,
    LOCK,
    UNLOCK,
    STAR,
    HEART,
    CHECKMARK,
    UNKNOWN
}
enum class GuiButtonStyle {
    PRIMARY,
    SECONDARY,
    SUCCESS,
    WARNING,
    DANGER,
    GHOST,
    INVISIBLE
}
data class GuiButtonDefinition(
    val id: String,
    val alignment: Alignment,
    val icon: GuiIcon,

    var style: GuiButtonStyle = GuiButtonStyle.PRIMARY,

    val namedID: String? = null,

    val properties: Map<String, Any> = emptyMap(),

    val tooltip: List<Component> = emptyList(),
    val action: GuiButtonAction = GuiButtonAction.None,
    val state: GuiElementState = GuiElementState(),
)
data class GuiDefinition(
    val id: ResourceLocation,

    val type: GuiType,
    val layout: GuiLayout,

    val supportedEvents: Set<ResourceLocation> = emptySet(),
    val buttons: List<GuiButtonDefinition> = emptyList(),

    val properties: Map<String, Any> = emptyMap(),
    val onCreated: (GuiInstance) -> Unit = { }
)

data class GuiContentDefinition(
    val id: String,
    val title: Component,
    val tooltip: List<Component> = emptyList(),
    val state: GuiElementState = GuiElementState(),
    val properties: Map<String, Any?> = emptyMap()
)

object GuiRegistry: Registry<GuiDefinition, GuiRegistry>("gui_registry") {

    private val guis = mutableMapOf<ResourceLocation, GuiDefinition>()

    override fun register(definition: GuiDefinition) {
        if (definition.id in guis) {
            logger.warn("GUI ${definition.id} already registered")
            return
        }

        guis[definition.id] = definition
        logger.info("Registered Gui definition ${definition.id}")
    }

    fun get(id: ResourceLocation): GuiDefinition? {
        return guis[id]
    }

    fun contains(id: ResourceLocation): Boolean {
        return id in guis
    }

    /**
     *
     * @return An unmodifiable collection of all registered entities
     */
    override fun getRegisteredEntities(): Collection<GuiDefinition> {
        return Collections.unmodifiableCollection(guis.values)
    }
}

interface GuiButton {
    val id: String
    val title: Component
    val tooltip: List<Component>
    var state: GuiElementState
    val action: GuiButtonAction

    fun patchState(patch: GuiElementState.() -> GuiElementState)
    fun placeholders(): MutableMap<String, Any?>
}
interface GuiContent {
    val id: String
    val title: Component
    val tooltip: List<Component>
    var state: GuiElementState

    fun patchState(patch: GuiElementState.() -> GuiElementState)
    fun placeholders(): Map<String, Any?>
}
interface GuiInstance {
    val id: UUID
    val player: PlatformPlayer
    val type: GuiType
    var title: Component?
    val state: MutableMap<String, Any>
    val definition: GuiDefinition

    fun getButton(id: String): GuiButton?
    fun removeButton(id: String)
    fun getButtonSlot(id: String): Int?
    fun clickButton(id: String)
    fun hasButton(id: String): Boolean

    operator fun get(slot: Int): GuiContent? = getContent(slot)
    operator fun set(slot: Int, value: GuiContentDefinition?) = setContent(slot, value)
    fun setContent(slot: Int, item: GuiContentDefinition?)
    fun getContent(slot: Int): GuiContent?
    fun getContents(): Map<Int, GuiContent>
    fun clearContents()

    fun updateElement(id: String)
    fun updateElement(button: GuiButton)
    fun updateElement(content: GuiContent)
    fun updateAll()

    fun size(): Int
    fun close()
    fun update()

    fun sendProperty(key: String, value: Any)
    fun <T: GuiEvent> on(eventName: ResourceLocation, listener: (T) -> Unit)
    fun emit(event: GuiEvent): GuiEvent

    fun setLoading(loading: Boolean)

    fun isLoading(): Boolean

    fun beginRequest(requestId: UUID)

    fun finishRequest(requestId: UUID)

    fun isCurrentRequest(requestId: UUID): Boolean

    fun requestData(
        reason: GuiEvent.DataRequested.Reason,
        page: Int? = null,
        query: String? = null,
        sorting: String? = null,
        filters: Map<String, Any?> = emptyMap(),
        payload: Map<String, Any?> = emptyMap()
    ): UUID
}
abstract class BaseGuiInstance(
    override val id: UUID,
    override val player: PlatformPlayer,
    override val type: GuiType,
    override var title: Component?
) : GuiInstance
{

    private val listeners = mutableMapOf<ResourceLocation, MutableList<(GuiEvent) -> Unit>>()
    private val activeRequestId = java.util.concurrent.atomic.AtomicReference<UUID?>(null)
    @Volatile private var loading = false

    override fun <T: GuiEvent> on(eventName: ResourceLocation, listener: (T) -> Unit) {
        listeners.getOrPut(eventName) { mutableListOf() }.add(listener as (GuiEvent) -> Unit)
    }

    override fun emit(event: GuiEvent): GuiEvent {
        listeners[event.id]?.forEach { it(event) }

        if (event is GuiEvent.Custom) {
            listeners[event.id]?.forEach { it(event) }
        }

        if (event is GuiEvent.DeferredGuiEvent && event.isDeferred()) {
            beginRequest(event.requestId)
        }

        return event
    }

    override fun setLoading(loading: Boolean) {
        this.loading = loading
        applyLoadingState(loading)
    }

    override fun isLoading(): Boolean = loading

    override fun beginRequest(requestId: UUID) {
        activeRequestId.set(requestId)
        setLoading(true)
    }

    override fun finishRequest(requestId: UUID) {
        if (activeRequestId.get() == requestId) {
            activeRequestId.compareAndSet(requestId, null)
            setLoading(false)
        }
    }

    override fun isCurrentRequest(requestId: UUID): Boolean {
        return activeRequestId.get() == requestId
    }

    override fun requestData(
        reason: GuiEvent.DataRequested.Reason,
        page: Int?,
        query: String?,
        sorting: String?,
        filters: Map<String, Any?>,
        payload: Map<String, Any?>
    ): UUID {
        val event = GuiEvent.DataRequested(
            gui = this,
            reason = reason,
            page = page,
            query = query,
            sorting = sorting,
            filters = filters,
            payload = payload
        )

        emit(event)
        return event.requestId
    }

    protected abstract fun applyLoadingState(loading: Boolean)
    protected fun resolvePlaceholders(
        text: Component,
        placeholders: Map<String, Any?>
    ): Component {
        val config = TextReplacementConfig.builder()

        placeholders.forEach { (key, value) ->
            config.match("{$key}")
            config.replacement(value?.toString() ?: "null")
        }

        return text.replaceText(config.build())
    }
}
interface GuiInterpreter {
    fun supports(type: GuiType): Boolean {
        if (type is GuiType.Custom) {
            return GuiRegistry.contains(type.id)
        }
        return true
    }
    fun open(
        player: PlatformPlayer,
        type: GuiType,
        title: Component? = null
    ): GuiInstance
}

object DefaultGuis {
    fun register() {
        GuiRegistry.register(
            GuiDefinition(
                id = rl("core:music_gui_pieces"),
                type = GuiType.Custom(rl("core:music_gui_pieces")),
                layout = GuiLayout.Paged(
                    rows = 6,
                    nextButton = GuiButtonDefinition(
                        id = "next_page",
                        alignment = Alignment.BOTTOM_LEFT,
                        icon = GuiIcon.NEXT
                    ),
                    previousButton = GuiButtonDefinition(
                        id = "previous_page",
                        alignment = Alignment.BOTTOM_RIGHT,
                        icon = GuiIcon.PREVIOUS
                    ),

                    contentSlots = (9..37).toList()
                ),
                buttons = listOf(
                    GuiButtonDefinition(
                        id = "sort_button",
                        alignment = Alignment.TOP_RIGHT,
                        icon = GuiIcon.SORT,
                        namedID = "Sort Button",
                        tooltip = listOf(
                            Component.text("Current Sorting: ")
                                .append { Component.text("{current_sort}", NamedTextColor.GOLD) }
                        ),
                        action = GuiButtonAction.RequestData(
                            reason = GuiEvent.DataRequested.Reason.SORT_CHANGE
                        ),
                        state = GuiElementState(
                            metadata = mapOf(
                                "sort_cycle" to listOf("name_ascending", "name_descending", "time_ascending", "time_descending"),
                                "current_sort" to "name_ascending"
                            )
                        )
                    )
                )
            ) { gui ->
                gui.on<GuiEvent.ButtonClicked>(rl("gui:button_clicked")) { event ->
                    if (event.button.id == "sort_button") {
                        val sort = (event.button.state.metadata["sort_cycle"] as? List<String> ?: listOf()).getNextOrFirst(
                            event.button.state.metadata["current_sort"] as String
                        )
                        gui.state["sorting"] = sort
                        event.button.placeholders()["current_sort"] = sort.replace("_", "")
                            
                        gui.updateElement(event.button)
                    }
                }
                gui.on<GuiEvent.DataRequested>(rl("gui:data_requested")) { event ->
                    event.defer()
                    val requestId = event.requestId
                    when (event.reason) {
                        GuiEvent.DataRequested.Reason.SORT_CHANGE -> PlatformPlugin.scheduler().runAsync {
                            val sort = event.sorting ?: gui.state["sorting"] as? String ?: "name_ascending"
                            val page = event.page ?: gui.state["page"] as? Int ?: 0

                            val result = MusicPlayerDAO.INSTANCE.loadOwnedPieces(
                                gui.player.uuid.toString(), page, 36,
                                sort, event.query, event.filters
                            )

                            PlatformPlugin.scheduler().runMain {
                                if (!gui.isCurrentRequest(requestId)) return@runMain

                                gui.state["page"] = page
                                gui.state["totalPages"] = MusicPlayerDAO.INSTANCE
                                    .numberOfOwned(gui.player.uuid.toString()) % 36
                                gui.state["sorting"] = sort
                                gui.state["query"] = event.query ?: ""

                                gui.clearContents()
                                result.forEachIndexed { index, piece ->
                                    gui[index] = GuiContentDefinition(
                                        piece.musicPiece.id,
                                        Component.text(
                                            piece.musicPiece.name,
                                            genreColors[piece.musicPiece.genre] ?: TextColor.color(0xe5e49d)
                                        ),
                                        tooltip = listOf(
                                            Component.text("Authors: ", NamedTextColor.GRAY)
                                                .append {
                                                    val comp = Component.text()
                                                    piece.musicPiece.authors.forEachIndexed { index, author ->
                                                        comp.append(Component.text(author, NamedTextColor.GOLD))
                                                        if (index < piece.musicPiece.authors.lastIndex) {
                                                            comp.append(Component.text(", ", NamedTextColor.GRAY))
                                                        }
                                                    }
                                                    comp.build()
                                                }
                                        )
                                    )
                                }

                                gui.getButton("sort_button")?.patchState {
                                    copy(loading = false, disabled = false)
                                }

                                gui.finishRequest(requestId)
                                gui.setLoading(false)
                                gui.updateAll()
                            }
                        }
                        else -> {
                            gui.finishRequest(requestId)
                            gui.setLoading(false)
                            gui.updateAll()
                        }
                    }
                }
            }
        )
    }
}