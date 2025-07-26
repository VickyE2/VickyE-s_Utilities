/* Params */
const SLICE_WIDTH = 256;
const SLICE_GAP = 0;
const textureStages = [];
const overlayStagesNormal = [];
const overlayStagesDouble = [];
const stageMap = new Map();
const stageAnimatedCheckbox = document.getElementById('stageAnimatedCheckbox');
const animatedStageFilesContainer = document.getElementById('animatedStageFilesContainer');
const stageAnimationConfig = document.getElementById('stageAnimationConfig');
const animatedStageFilesInput = document.getElementById('animatedStageFiles');
const animatedStageFilesList = document.getElementById('animatedStageFilesList');
const openModalButton = document.getElementById('openModal');
const modalOverlay = document.getElementById('modalOverlay');
const closeModalButton = document.getElementById('closeModal');
const stageForm = document.getElementById('stageForm');
const overlayStageNormalDiv = document.getElementById('overlayStageNormal');
const overlayStageDoubleDiv = document.getElementById('overlayStageDouble');
const bossbarTypeSelect = document.getElementById('bossbarType');
const textureInput = document.getElementById('textureInput');
const overlayInput = document.getElementById('overlayInput');
const overlayInputLeft = document.getElementById('overlayInputLeft');
const overlayInputRight = document.getElementById('overlayInputRight');
const singleOverlayGroup = document.getElementById('singleOverlayGroup');
const doubleOverlayGroup = document.getElementById('doubleOverlayGroup');
const healthSlider = document.getElementById('healthSlider');
const healthValue = document.getElementById('healthValue');
const reversedCheckbox = document.getElementById('reversedCheckbox');
const offsetXInput = document.getElementById('offsetX');
const offsetYInput = document.getElementById('offsetY');
const resetHealthBarOnNewStage = document.getElementById('resetHBONS');
const offsetLeftXInput = document.getElementById('offsetLeftX');
const offsetLeftYInput = document.getElementById('offsetLeftY');
const offsetRightXInput = document.getElementById('offsetRightX');
const offsetRightYInput = document.getElementById('offsetRightY');
const textureImage = document.getElementById('textureImage');
const textureHolder = document.getElementById('textureHolder');
const overlayImage = document.getElementById('overlayImage');
const overlayImageLeft = document.getElementById('overlayImageLeft');
const overlayImageRight = document.getElementById('overlayImageRight');
const bossbarDisplay = document.getElementById('bossbarDisplay');
const animatedCheckbox = document.getElementById('animatedCheckbox');
const animatedFilesListContainer = document.getElementById('durationInputContainer');
const animatedFilesInput = document.getElementById('animatedFiles');
const animatedFilesList = document.getElementById('animatedFilesList');
const startAnimationBtn = document.getElementById('startAnimationBtn');
const animatedImageEl = document.getElementById('animatedImage');
const bossName = document.getElementById('bossName');
const healthSplits = document.getElementById('splits');
const layoutMap = new Map();
const textureStageContainer = document.getElementById("textureStageContainer");
let animType = document.getElementById('animType').value;
let duration = parseFloat(document.getElementById('animDuration').value);
let xEquation = document.getElementById('xEquation').value;
let yEquation = document.getElementById('yEquation').value;
let actualName = bossName.value.toLowerCase();
let nextStageThreashold = 0;
let halfWidth = 0;
let SCALE_FACTOR = 1;
let animatedMainFrames = '';
let animationHandler = null;
let mathAnimationHandler = null;
let defaultMainTextureAnimation = null;
let defaultOverlayDouble = null;
let defaultOverlayNormal = null;
let hasMadeTable = false;
let stageIntervals = [];

/* Tasks */
setInterval(() => {
    if (hasMadeTable == true) {
        let files = Array.from(document.getElementById("animatedFiles").files);
        if (files.length === 0) {
            console.error("No files in animatedFiles input.");
            return;
        }

        Promise.all(
            files.map((file, index) => {
                // Get duration and order inputs for each file.
                const durationInput = document.getElementById("duration_" + index);
                const orderInput = document.getElementById("order_" + index);
                const duration = parseFloat(durationInput.value) || 1;
                const order = parseFloat(orderInput.value) || (index + 1);
                return readFileAsDataURL(file, false).then(dataURL => ({
                    src: dataURL,
                    duration,
                    order,
                }));
            })
        ).then(frames => {
            frames.sort((a, b) => a.order - b.order);
            defaultMainTextureAnimation = { animatedFrames: frames };
        }).catch(err => {
            console.error("Error processing animated files:", err);
        });
    }
}, 1000);

/* Event Listeners */
/* <Add stage Form> */
stageAnimatedCheckbox.addEventListener("change", () => {
    if (stageAnimatedCheckbox.checked) {
        animatedStageFilesContainer.classList.remove("hidden");
        stageAnimationConfig.classList.remove("hidden");
        textureStageContainer.classList.add("hidden");
    }
    else {
        animatedStageFilesContainer.classList.add("hidden");
        stageAnimationConfig.classList.add("hidden");
        textureStageContainer.classList.remove("hidden");
    }
});
document.getElementById("scaleFactor").addEventListener("change", () => {
    SCALE_FACTOR = document.getElementById("scaleFactor").value;
});
animatedStageFilesInput.addEventListener('change', () => {
    animatedStageFilesList.innerHTML = '';
    const files = animatedStageFilesInput.files;
    const table = document.createElement('table');
    const header = document.createElement('tr');
    header.innerHTML = '<th>File Name</th><th>Duration (ticks)</th><th>Order</th>';
    table.appendChild(header);
    for (let i = 0; i < files.length; i++) {
        const row = document.createElement('tr');
        const nameCell = document.createElement('td');
        nameCell.textContent = files[i].name;
        const durationCell = document.createElement('td');
        const durationInput = document.createElement('input');
        durationInput.type = 'number';
        durationInput.value = '1';
        durationInput.min = '1';
        durationInput.id = 'stage_duration_' + i;
        durationCell.appendChild(durationInput);
        const orderCell = document.createElement('td');
        const orderInput = document.createElement('input');
        orderInput.type = 'number';
        orderInput.value = i + 1;
        orderInput.min = '1';
        orderInput.id = 'stage_order_' + i;
        orderCell.appendChild(orderInput);
        row.appendChild(nameCell);
        row.appendChild(durationCell);
        row.appendChild(orderCell);
        table.appendChild(row);
    }
    animatedStageFilesList.appendChild(table);
});
stageForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    let actualName = bossName.value.toLowerCase();
    const threshold = parseFloat(document.getElementById("stageThreshold").value);
    stageIntervals.push(threshold / 100);

    let stageObj = {
        threshold: threshold,
        animated: false,
        images: []
    };

    const stageAnimatedCheckbox = document.getElementById("stageAnimatedCheckbox");

    if (bossbarTypeSelect.value === "double") {
        const overlayLeftFile = document.getElementById("overlayStageLeft").files[0];
        const overlayRightFile = document.getElementById("overlayStageRight").files[0];
        if (overlayLeftFile && overlayRightFile) {
            console.log("Left: " + overlayLeftFile + " Right: " + overlayRightFile);
            Promise.all([
                readFileAsDataURL(overlayLeftFile),
                readFileAsDataURL(overlayRightFile)
            ]).then(([leftDataURL, rightDataURL]) => {
                let data = {
                    threshold,
                    percentage_tre: document.getElementById("stagePercentageThreshold").value,
                    left: leftDataURL,
                    right: rightDataURL
                };
                console.log("Data: ", data);
                overlayStagesDouble.push(data);
                modalOverlay.classList.add("hidden");
                updateStageImages();
            }).catch(err => {
                console.error("Error reading overlay files:", err);
                modalOverlay.classList.add("hidden");
                updateStageImages();
            });
        }
        else {
            console.error("A Fatal Error has occurred (double mode overlay missing). Left: " + overlayLeftFile + " Right: " + overlayRightFile);
            modalOverlay.classList.add("hidden");
            updateStageImages();
        }
    }
    else {
        // Single mode.
        const overlayFile = document.getElementById("overlayStageFile").files[0];
        if (overlayFile) {
            readFileAsDataURL(overlayFile, (overlayDataURL) => {
                overlayStagesNormal.push({
                    threshold,
                    src: overlayDataURL
                });
                modalOverlay.classList.add("hidden");
                updateStageImages();
            });
        }
        else {
            console.error("A Fatal Error has occurred (normal mode overlay missing).");
            modalOverlay.classList.add("hidden");
            updateStageImages();
        }
    }

    if (stageAnimatedCheckbox.checked) {
        stageObj.animated = true;
        // Process animated stage files.
        const animatedStageFilesInput = document.getElementById("animatedStageFiles");
        if (animatedStageFilesInput.files.length === 0) {
            alert("Please select animated stage frames.");
            return;
        }
        let frames = [];
        let fileReadPromises = [];
        for (let i = 0; i < animatedStageFilesInput.files.length; i++) {
            const file = animatedStageFilesInput.files[i];
            const durationInput = document.getElementById("stage_duration_" + i);
            const orderInput = document.getElementById("stage_order_" + i);
            const duration = parseFloat(durationInput.value) || 1;
            const order = parseFloat(orderInput.value) || (i + 1);
            fileReadPromises.push(
                readFileAsDataURL(file).then((dataURL) => {
                    // Generate a fileName for packaging.
                    const fileName = actualName + "_stage_" + threshold + "_" + i.toString().padStart(1, "0") + ".png";
                    frames.push({
                        src: dataURL,
                        duration: duration,
                        order: order,
                        fileName: fileName
                    });
                })
            );
        }
        await Promise.all(fileReadPromises);
        frames.sort((a, b) => a.order - b.order);
        stageObj.animatedFrames = frames;
        stageObj.animConfig = {
            type: document.getElementById("stageAnimType").value,
            duration: document.getElementById("stageAnimDuration").value,
            xEquation: document.getElementById("stageAnimXEquation").value,
            yEquation: document.getElementById("stageAnimYEquation").value
        };
        // For layout purposes, we reference the animated stage by a special name.
        stageObj.images.push({
            name: "animated_stage_" + threshold,
            x: 0,
            y: 0
        });
        textureStages.push(stageObj);
        modalOverlay.classList.add("hidden");
        updateStageImages();
    }
    else {
        // Non-animated stage: Process texture stage file.
        const textureFileInput = document.getElementById("textureStageFile");
        if (textureFileInput.files.length > 0) {
            readFileAsDataURL(textureFileInput.files[0], (textureDataURL) => {
                textureStages.push({
                    threshold,
                    src: textureDataURL
                });
            });
        }
    }

    e.target.reset();
});
openModalButton.addEventListener('click', () => {
    if (bossbarTypeSelect.value === 'double') {
        overlayStageNormalDiv.classList.add('hidden');
        overlayStageDoubleDiv.classList.remove('hidden');
    }
    else {
        overlayStageNormalDiv.classList.remove('hidden');
        overlayStageDoubleDiv.classList.add('hidden');
    }
    modalOverlay.classList.remove('hidden');
});
closeModalButton.addEventListener('click', () => {
    modalOverlay.classList.add('hidden');
});
/* <Edit stage Form> */
document.getElementById("editStageAnimatedCheckbox").addEventListener("change", () => {
    if (stageAnimatedCheckbox.checked) {
        document.getElementById("editAnimatedStageFilesContainer").classList.remove("hidden");
        document.getElementById("editStageAnimationConfig").classList.remove("hidden");
        document.getElementById("editTextureStageContainer").classList.add("hidden");
    }
    else {
        document.getElementById("editAnimatedStageFilesContainer").classList.add("hidden");
        document.getElementById("editStageAnimationConfig").classList.add("hidden");
        document.getElementById("editTextureStageContainer").classList.remove("hidden");
    }
});
document.getElementById("editAnimatedStageFiles").addEventListener('change', () => {
    document.getElementById("editAnimatedStageFiles").innerHTML = '';
    const files = document.getElementById("editAnimatedStageFilesInput").files;
    const table = document.createElement('table');
    const header = document.createElement('tr');
    header.innerHTML = '<th>File Name</th><th>Duration (ticks)</th><th>Order</th>';
    table.appendChild(header);
    for (let i = 0; i < files.length; i++) {
        const row = document.createElement('tr');
        const nameCell = document.createElement('td');
        nameCell.textContent = files[i].name;
        const durationCell = document.createElement('td');
        const durationInput = document.createElement('input');
        durationInput.type = 'number';
        durationInput.value = '1';
        durationInput.min = '1';
        durationInput.id = 'stage_duration_' + i;
        durationCell.appendChild(durationInput);
        const orderCell = document.createElement('td');
        const orderInput = document.createElement('input');
        orderInput.type = 'number';
        orderInput.value = i + 1;
        orderInput.min = '1';
        orderInput.id = 'stage_order_' + i;
        orderCell.appendChild(orderInput);
        row.appendChild(nameCell);
        row.appendChild(durationCell);
        row.appendChild(orderCell);
        table.appendChild(row);
    }
    document.getElementById("editAnimatedStageFiles").appendChild(table);
});
document.getElementById("editStageForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    actualName = bossName.value.toLowerCase();
    const editMO = document.getElementById('editStageModal');

    // Assume the stage key is stored in a data attribute (e.g. data-stage-key)
    const stageKey = e.target.dataset.stageKey;
    if (!stageKey) {
        console.error("No stage key provided for editing.");
        return;
    }

    const threshold = parseFloat(document.getElementById("editStageThreshold").value);
    console.log("[Edit Stage] New threshold:", threshold);

    // Create an updated stage object.
    let stageObj = {
        threshold: threshold,
        animated: false,
        images: [] // For layout YAML
    };

    // Determine if the stage is animated.
    const stageAnimatedCheckbox = document.getElementById("editStageAnimatedCheckbox");

    // Process overlay for this stage (applies to both animated and non-animated)
    if (bossbarTypeSelect.value === "double") {
        const overlayLeftFile = document.getElementById("editOverlayStageLeft").files[0];
        const overlayRightFile = document.getElementById("editOverlayStageRight").files[0];
        if (overlayLeftFile && overlayRightFile) {
            console.log("[Edit Stage] Double mode overlay files provided.");
            try {
                const [leftDataURL, rightDataURL] = await Promise.all([
                    readFileAsDataURL(overlayLeftFile),
                    readFileAsDataURL(overlayRightFile)
                ]);
                let data = {
                    threshold,
                    percentage_tre: document.getElementById("stagePercentageThreshold").value,
                    left: leftDataURL,
                    right: rightDataURL
                };
                console.log("[Edit Stage] Overlay data (double):", data);
                // Replace any existing overlay for this threshold in overlayStagesDouble.
                overlayStagesDouble = overlayStagesDouble.filter(s => s.threshold !== threshold);
                overlayStagesDouble.push(data);
            } catch (err) {
                console.error("[Edit Stage] Error reading double overlay files:", err);
            }
        } else {
            console.error("[Edit Stage] Missing double overlay files.");
        }
    } else {
        // Single mode.
        const overlayFile = document.getElementById("editOverlayStageFile").files[0];
        if (overlayFile) {
            try {
                const overlayDataURL = await readFileAsDataURL(overlayFile);
                console.log("[Edit Stage] Overlay data (single) loaded.");
                overlayStagesNormal = overlayStagesNormal.filter(s => s.threshold !== threshold);
                overlayStagesNormal.push({ threshold, src: overlayDataURL });
            } catch (err) {
                console.error("[Edit Stage] Error reading single overlay file:", err);
            }
        } else {
            console.error("[Edit Stage] Missing single overlay file.");
        }
    }

    if (stageAnimatedCheckbox.checked) {
        stageObj.animated = true;
        // Process animated stage files.
        const animatedStageFilesInput = document.getElementById("editAnimatedStageFiles");
        if (animatedStageFilesInput.files.length === 0) {
            alert("Please select animated stage frames.");
            return;
        }
        let frames = [];
        let fileReadPromises = [];
        for (let i = 0; i < animatedStageFilesInput.files.length; i++) {
            const file = animatedStageFilesInput.files[i];
            const durationInput = document.getElementById("editStage_duration_" + i);
            const orderInput = document.getElementById("editStage_order_" + i);
            const duration = parseFloat(durationInput.value) || 1;
            const order = parseFloat(orderInput.value) || (i + 1);
            fileReadPromises.push(
                readFileAsDataURL(file).then((dataURL) => {
                    const fileName = actualName + "_stage_" + threshold + "_" + i.toString().padStart(1, "0") + ".png";
                    frames.push({
                        src: dataURL,
                        duration: duration,
                        order: order,
                        fileName: fileName
                    });
                })
            );
        }
        await Promise.all(fileReadPromises);
        frames.sort((a, b) => a.order - b.order);
        stageObj.animatedFrames = frames;
        stageObj.animConfig = {
            type: document.getElementById("editStageAnimType").value,
            duration: document.getElementById("editStageAnimDuration").value,
            xEquation: document.getElementById("editStageAnimXEquation").value,
            yEquation: document.getElementById("editStageAnimYEquation").value
        };
        // For layout purposes, we reference the animated stage by a special name.
        stageObj.images.push({
            name: "animated_stage_" + threshold,
            x: 0,
            y: 0
        });
        console.log("[Edit Stage] Processed animated stage:", stageObj);
    }
    else {
        // Non-animated stage: Process texture stage file.
        const textureFileInput = document.getElementById("editTextureStageFile");
        if (textureFileInput.files.length > 0) {
            try {
                const textureDataURL = await readFileAsDataURL(textureFileInput.files[0]);
                stageObj.src = textureDataURL;
                console.log("[Edit Stage] Processed static texture stage.");
            } catch (err) {
                console.error("[Edit Stage] Error reading static texture stage file:", err);
            }
        }
    }

    // Now update the stage in textureStages.
    // Assume textureStages is an array and we want to update the stage with key equal to stageKey.
    let updated = false;
    for (let i = 0; i < textureStages.length; i++) {
        // For example, you might compare the threshold or some unique identifier.
        if (textureStages[i].threshold === parseFloat(stageKey.split("_")[2])) { // e.g. key "boss_default" or "boss_stage_50"
            textureStages[i] = stageObj;
            updated = true;
            console.log("[Edit Stage] Stage updated at index", i);
            break;
        }
    }
    if (!updated) {
        console.warn("[Edit Stage] Stage not found; adding as new stage.");
        textureStages.push(stageObj);
    }

    editMO.classList.add("hidden");
    updateStageImages();
    e.target.reset();
});
document.getElementById('cancelEdit').addEventListener('click', () => {
    document.getElementById('editStageModal').classList.add('hidden');
});
animatedCheckbox.addEventListener('change', () => {
    if (animatedCheckbox.checked) {
        animatedFilesListContainer.classList.remove('hidden');
        document.getElementById('textureHolder').classList.add('hidden');
    } else {
        animatedFilesListContainer.classList.add('hidden');
        document.getElementById('textureHolder').classList.remove('hidden');
    }
});
animatedFilesInput.addEventListener('change', () => {
    animatedFilesList.innerHTML = '';
    let files = animatedFilesInput.files;
    const table = document.createElement('table');
    const header = document.createElement('tr');
    const thFile = document.createElement('th');
    thFile.textContent = 'File Name';
    const thDuration = document.createElement('th');
    thDuration.textContent = 'Duration (ticks)';
    const thOrder = document.createElement('th');
    thOrder.textContent = 'Order';
    header.appendChild(thFile);
    header.appendChild(thDuration);
    header.appendChild(thOrder);
    table.appendChild(header);
    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const row = document.createElement('tr');
        const nameCell = document.createElement('td');
        nameCell.textContent = file.name;
        row.appendChild(nameCell);
        const durationCell = document.createElement('td');
        const durationInput = document.createElement('input');
        durationInput.type = 'number';
        durationInput.value = '1';
        durationInput.min = '1';
        durationInput.style.width = '60px';
        durationInput.id = 'duration_' + i;
        durationCell.appendChild(durationInput);
        row.appendChild(durationCell);
        const orderCell = document.createElement('td');
        const orderInput = document.createElement('input');
        orderInput.type = 'number';
        orderInput.value = i + 1;
        orderInput.min = '1';
        orderInput.style.width = '60px';
        orderInput.id = 'order_' + i;
        orderCell.appendChild(orderInput);
        row.appendChild(orderCell);
        table.appendChild(row);
    }
    animatedFilesList.appendChild(table);
    hasMadeTable = true;
});
startAnimationBtn.addEventListener('click', async () => {
    const files = animatedFilesInput.files;
    let actualName = bossName.value.toLowerCase();
    console.log("[Animation] Starting animation for:", actualName);

    const frames = [];
    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const durationInput = document.getElementById('duration_' + i);
        const orderInput = document.getElementById('order_' + i);
        const duration = parseFloat(durationInput.value) || 1;
        const order = parseFloat(orderInput.value) || (i + 1);
        console.log(`[Animation] Processing file "${file.name}" with duration=${duration} and order=${order}`);
        try {
            const dataURL = await readFileAsDataURL(file);
            const fileName = actualName + '_amt_' + i.toString().padStart(2, '0') + '.png';
            frames.push({
                src: dataURL,
                duration: duration,
                order: order,
                fileName: fileName
            });
            console.log(`[Animation] File processed: ${fileName}`);
        } catch (e) {
            console.error('[Animation] Error reading file:', file.name, e);
        }
    }

    // Sort frames by their order property (ascending).
    frames.sort((a, b) => a.order - b.order);
    console.log("[Animation] Sorted frames:", frames);

    if (frames.length > 0) {
        animatedMainFrames = frames;
        if (animationHandler != null) animationHandler.stop();
        animationHandler = animateImage(frames, animatedImageEl);
        console.log("[Animation] Animation started with", frames.length, "frames.");
    }
    else {
        console.warn("[Animation] No frames processed, animation not started.");
    }
});
window.addEventListener('DOMContentLoaded', renderTextureStageList);
textureInput.addEventListener('change', () => {
    loadLocalImage(textureInput, textureImage, updateScale);
    updateOverlayCrop();
});
overlayInput.addEventListener('change', () => {
    loadLocalImage(overlayInput, overlayImage, updateScale);
    updateOverlayCrop();
});
overlayInputLeft.addEventListener('change', () => {
    loadLocalImage(overlayInputLeft, overlayImageLeft, updateScale);
    updateOverlayCrop();
});
overlayInputRight.addEventListener('change', () => {
    loadLocalImage(overlayInputRight, overlayImageRight, updateScale);
    updateOverlayCrop();
});
bossbarTypeSelect.addEventListener('change', () => {
    if (bossbarTypeSelect.value === 'double') {
        singleOverlayGroup.style.display = 'none';
        doubleOverlayGroup.style.display = 'flex';
        document.getElementById('doubleOffsetGroup').style.display = 'flex';
    }
    else {
        singleOverlayGroup.style.display = 'flex';
        doubleOverlayGroup.style.display = 'none';
        document.getElementById('doubleOffsetGroup').style.display = 'none';
    }
    updateOverlayCrop();
});
healthSlider.addEventListener('input', () => {
    healthValue.textContent = healthSlider.value + '%';
    updateOverlayCrop();
    updateStageImages();
});
reversedCheckbox.addEventListener('change', updateOverlayCrop);
document.getElementById('bossName').addEventListener('change', () => {
    let actualName = bossName.value.toLowerCase();
});
document.getElementById('playAnimationButton').addEventListener('click', () => {
    const healthPercent = parseFloat(healthSlider.value);
    let stage = getNextStage(textureStages, healthPercent);
    if (stage && stage.animated && stage.animConfig) {
        if (mathAnimationHandler != null) mathAnimationHandler.stop();
        mathAnimationHandler = playAnimation(stage.animConfig);
    }
    else {
        const animConfig = {
            'type': document.getElementById('animType').value,
            'duration': parseFloat(document.getElementById('animDuration').value),
            'x-equation': document.getElementById('xEquation').value,
            'y-equation': document.getElementById('yEquation').value,
        };
        stage.animConfig = animConfig;
        if (mathAnimationHandler != null) mathAnimationHandler.stop();
        mathAnimationHandler = playAnimation(animConfig);
    }
});
document.getElementById('downloadZip').addEventListener('click', generateZip);
document.getElementById("overlayInputLeft").addEventListener("change", () => {
    const file = document.getElementById("overlayInputLeft").files[0];
    if (file) {
        readFileAsDataURL(file).then((dataURL) => {
            defaultOverlayDouble = defaultOverlayDouble || {};
            defaultOverlayDouble.left = dataURL;
            console.log("Default overlay left set.");
        });
    }
});
document.getElementById("overlayInputRight").addEventListener("change", () => {
    const file = document.getElementById("overlayInputRight").files[0];
    if (file) {
        readFileAsDataURL(file).then((dataURL) => {
            defaultOverlayDouble = defaultOverlayDouble || {};
            defaultOverlayDouble.right = dataURL;
            console.log("Default overlay right set.");
        });
    }
});
document.getElementById("overlayInput").addEventListener("change", () => {
    const file = document.getElementById("overlayInput").files[0];
    if (file) {
        readFileAsDataURL(file).then((dataURL) => {
            defaultOverlayNormal = { src: dataURL };
            console.log("Default overlay (normal) set.");
        });
    }
});
document.getElementById("updateStagesButton").addEventListener("click", updateStageList);

/* Initial Method calls */
updateOverlayCrop();

/* Functions */
function dataURLtoBlob(dataurl) {
    const arr = dataurl.split(',');
    const mimeMatch = arr[0].match(/:(.*?);/);
    if (!mimeMatch) return null;
    const mime = mimeMatch[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], {
        type: mime
    });
}
function updateStageList() {
    console.log("Updating stage list...");
    const tableBody = document.querySelector("#textureStageTable tbody");
    tableBody.innerHTML = ""; // Clear previous rows

    // Iterate over textureStages array.
    textureStages.forEach((stage, index) => {
        const row = document.createElement("tr");

        // Create cell for threshold.
        const thresholdCell = document.createElement("td");
        thresholdCell.textContent = stage.threshold;
        row.appendChild(thresholdCell);

        // Create cell for image preview.
        const previewCell = document.createElement("td");
        const previewImg = document.createElement("img");
        // If the stage is animated and has frames, use the first frame; otherwise, use the static image.
        if (stage.animated && stage.animatedFrames && stage.animatedFrames.length > 0) {
            previewImg.src = stage.animatedFrames[0].src;
        }
        else {
            previewImg.src = stage.src || "";
        }
        previewImg.style.maxWidth = "100px";
        previewImg.style.maxHeight = "50px";
        previewCell.appendChild(previewImg);
        row.appendChild(previewCell);

        // Create cell for actions.
        const actionCell = document.createElement("td");
        const editButton = document.createElement("button");
        editButton.textContent = "Edit";
        // Use the index as the key. Adjust if you use a unique key.
        editButton.addEventListener("click", () => openEditModal(index));
        actionCell.appendChild(editButton);
        row.appendChild(actionCell);

        tableBody.appendChild(row);
    });

    console.log("Stage list updated.");
}
function openEditModal(stageKey) {
    // Retrieve the stage data from your global stageMap (or array) using the primary key.
    const stageObj = textureStages[stageKey];
    if (!stageObj) {
        console.error("Stage not found for key:", stageKey);
        return;
    }

    // Populate the threshold input.
    document.getElementById("editStageThreshold").value = stageObj.threshold;

    // For non-animated stages, display a preview of the current texture.
    if (!stageObj.animated) {
        // For example, set a preview image element’s src.
        if (stageObj.src) {
            document.getElementById("editTexturePreview").src = stageObj.src;
        }
        // Hide the animated fields.
        document.getElementById("editStageAnimatedCheckbox").checked = false;
        document.getElementById("editAnimatedStageFilesContainer").classList.add("hidden");
        document.getElementById("editStageAnimationConfig").classList.add("hidden");
    } else {
        // For animated stages, check the animated checkbox.
        document.getElementById("editStageAnimatedCheckbox").checked = true;
        // Show the animated stage file inputs and animation config fields.
        document.getElementById("editAnimatedStageFilesContainer").classList.remove("hidden");
        document.getElementById("editStageAnimationConfig").classList.remove("hidden");

        // Populate the animation configuration fields.
        if (stageObj.animConfig) {
            document.getElementById("editStageAnimType").value = stageObj.animConfig.type;
            document.getElementById("editStageAnimDuration").value = stageObj.animConfig.duration;
            document.getElementById("editStageAnimXEquation").value = stageObj.animConfig.xEquation;
            document.getElementById("editStageAnimYEquation").value = stageObj.animConfig.yEquation;
        }

        // Optionally, list the animated frames (since you can't set file inputs, you might display previews).
        console.log("Animated frames for editing:", stageObj.animatedFrames);
        document.getElementById('editAnimatedStageFilesList').innerHTML = '';
        let files = stageObj.animatedFrames;
        const table = document.createElement('table');
        const header = document.createElement('tr');
        const thFile = document.createElement('th');
        thFile.textContent = 'File Name';
        const thDuration = document.createElement('th');
        thDuration.textContent = 'Duration (ticks)';
        const thOrder = document.createElement('th');
        thOrder.textContent = 'Order';
        header.appendChild(thFile);
        header.appendChild(thDuration);
        header.appendChild(thOrder);
        table.appendChild(header);
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const row = document.createElement('tr');
            const nameCell = document.createElement('td');
            nameCell.textContent = file.name;
            row.appendChild(nameCell);
            const durationCell = document.createElement('td');
            const durationInput = document.createElement('input');
            durationInput.type = 'number';
            durationInput.value = file.duration;
            durationInput.min = '1';
            durationInput.style.width = '60px';
            durationInput.id = 'edit_duration_' + i;
            durationCell.appendChild(durationInput);
            row.appendChild(durationCell);
            const orderCell = document.createElement('td');
            const orderInput = document.createElement('input');
            orderInput.type = 'number';
            orderInput.value = file.order;
            orderInput.min = '1';
            orderInput.style.width = '60px';
            orderInput.id = 'edit_order_' + i;
            orderCell.appendChild(orderInput);
            row.appendChild(orderCell);
            table.appendChild(row);
        }
        document.getElementById('editAnimatedStageFilesList').appendChild(table);
    }

    // If you have overlay data (for double mode, for example), populate preview elements here.
    // For example:
    if (stageObj.overlay) {
        document.getElementById("editOverlayPreview").src = stageObj.overlay;
    }

    // Store the stage key in a data attribute on the form so you know which stage to update on submit.
    document.getElementById("editStageForm").dataset.stageKey = stageKey;

    // Finally, show the edit modal.
    document.getElementById("editStageModal").classList.remove("hidden");
}
async function generateZip() {
    let actualName = bossName.value.toLowerCase();
    console.log("[ZIP] actualName:", actualName);

    const zip = new JSZip();
    const splitsAmount = healthSplits.value;
    console.log("[ZIP] splitsAmount:", splitsAmount);

    const assetsDir = zip.folder('assets/betterbossbars');
    const imagesDir = zip.folder('images');
    const popupDir = zip.folder('popups');
    const layoutDir = zip.folder('layouts');
    let FullImageYml = '';

    const loadImage = (src) =>
    new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(img);
        img.onerror = (e) => reject(e);
        img.src = src;
    });
    const processImage = async (src, baseName, onSegment) => {
        const img = await loadImage(src);
        const segmentWidth = 256;
        const segmentCount = Math.ceil(img.width / segmentWidth);

        const segments = await splitLargeImage(src, segmentWidth);

        segments.forEach((blob, i) => {
            const segmentName = `${baseName}_${i}`;
            if (blob) {
                assetsDir.file(`${segmentName}.png`, blob);
                console.log(`[ZIP] Actually split and saved segment: ${segmentName}.png`);
                onSegment(segmentName, i * segmentWidth);
            }
            else {
                console.warn(`❌ Failed to make blob for ${segmentName}`);
            }
        });

        return segmentCount; // Return segment count for tracking
    };

    // Process main texture (static or animated)
    if (!animatedCheckbox.checked) {
        console.log("[ZIP] Processing static sliced texture...");

        const defaultTextureData = textureImage.dataset.default || textureImage.src;
        halfWidth = textureImage.width / 2;
        if (defaultTextureData) {
            const imageEntries = [];
            const baseName = `${actualName}_ts_1`;

            const expectedSegments = Math.ceil(textureImage.naturalWidth / 256);
            const segmentCount = await processImage(defaultTextureData, baseName, (segmentName, xOffset) => {
                imageEntries.push({ name: segmentName, x: xOffset * SCALE_FACTOR, y: 0 * SCALE_FACTOR });
                FullImageYml += generateSingleImage(
                    segmentName,
                    `betterbossbars/${segmentName}.png`
                ) + "\n\n";
            });

            // Save blobs once after processImage
            for (let i = 0; i < segmentCount; i++) {
                const segName = `${baseName}_${i}`;
                assetsDir.file(`${segName}.png`, dataURLtoBlob(defaultTextureData));
            }

            if (imageEntries.length === expectedSegments) {
                stageMap.set(`${actualName}_default`, {
                    threshold: 1,
                    animated: false,
                    images: imageEntries
                });
                console.log("[ZIP] Set static sliced default stage in stageMap.");
            }
        }
    }
    else {
        console.log("[ZIP] Processing animated sliced texture...");
        const segmentGroups = new Map(); // index -> [frames for that segment]
        let totalSegmentsExpected = 0;
        let index = 0;
        for (const [frameIndex, frame] of animatedMainFrames.entries()) {
            index++;
            const baseName = `${actualName}_ts_1_frame_${frameIndex}`;

            // Await the segmentation and get the number of slices
            const segmentCount = await processImage(frame.src, baseName, (segmentName, xOffset) => {
                const sliceIndex = xOffset / 256;

                if (!segmentGroups.has(sliceIndex)) segmentGroups.set(sliceIndex, []);

                segmentGroups.get(sliceIndex).push({
                    fileName: `${segmentName}.png`,
                    duration: frame.duration
                });

                console.log(`[ZIP] sliceIndex: ${sliceIndex}, segments count: ${segmentGroups.get(sliceIndex).length}`);
            });

            // Update totalSegmentsExpected *after* processImage finishes for this frame
            totalSegmentsExpected = Math.max(totalSegmentsExpected, segmentCount);
            console.log(`[ZIP] totalSegmentsExpected updated: ${totalSegmentsExpected}, segmentGroups size: ${segmentGroups.size}`);

            // Now check your condition here (not inside the callback)
            if (
            [...segmentGroups.values()].every(frames => frames.length === animatedMainFrames.length) &&
            segmentGroups.size === totalSegmentsExpected
            ) {
                console.log("[ZIP] Condition met! Generating sequences...");

                segmentGroups.forEach((frames, index) => {
                    const seqName = `${actualName}_ts_1_${index}`;
                    FullImageYml += generateSequenceImage(seqName, frames) + "\n\n";
                });

                const images = [...segmentGroups.keys()].map((i) => ({
                    name: `${actualName}_ts_1_${i}`,
                    x: (i * 256) * SCALE_FACTOR,
                    y: 0
                }));

                stageMap.set(`${actualName}_default`, {
                    threshold: 1,
                    animated: true,
                    animConfig: { ...animatedMainFrames[0].animConfig },
                    animatedFrames: animatedMainFrames,
                    images
                });

                console.log("[ZIP] Set animated sliced default stage in stageMap.");
            }
        }
        halfWidth = (index * 256) / 2;
    }
    // Process texture stages.
    for (const [index, stage] of textureStages.entries()) {
        console.log("[ZIP] Processing texture stage:", stage.threshold);
        if (!stage.animated) {
            const imageEntries = [];
            const baseName = `${actualName}_ts_${stage.threshold}`;
            const expectedSegments = Math.ceil(stage.imageWidth / 256);

            const segmentCount = await processImage(stage.src, baseName, (segmentName, xOffset) => {
                imageEntries.push({ name: segmentName, x: xOffset * SCALE_FACTOR, y: 0 * SCALE_FACTOR });
                FullImageYml += generateSingleImage(
                    segmentName,
                    `betterbossbars/${segmentName}.png`
                ) + "\n\n";
            });

            // Save blobs for static stage
            for (let i = 0; i < segmentCount; i++) {
                const segName = `${baseName}_${i}`;
                assetsDir.file(`${segName}.png`, dataURLtoBlob(stage.src));
            }

            if (imageEntries.length === expectedSegments) {
                stageMap.set(`${actualName}_stage_${stage.threshold}`, {
                    threshold: stage.threshold,
                    animated: false,
                    images: imageEntries
                });
                console.log(`[ZIP] Set static stage in stageMap for threshold ${stage.threshold}.`);
            }
        }
        else {
            console.log("[ZIP] Processing animated sliced texture...");
            const segmentGroups = new Map(); // index -> [frames for that segment]
            let totalSegmentsExpected = 0;
            for (const [frameIndex, frame] of stage.animatedFrames.entries()) {
                const baseName = `${actualName}_ts_${stage.threshold}_frame_${frameIndex}`;
                // Await segmentation & get segment count
                const segmentCount = await processImage(frame.src, baseName, (segmentName, xOffset) => {
                    const sliceIndex = xOffset / 256;
                    if (!segmentGroups.has(sliceIndex)) segmentGroups.set(sliceIndex, []);
                    segmentGroups.get(sliceIndex).push({
                        fileName: `${segmentName}.png`,
                        duration: frame.duration
                    });
                });

                totalSegmentsExpected = Math.max(totalSegmentsExpected, segmentCount);
                console.log(`[ZIP] totalSegmentsExpected updated: ${totalSegmentsExpected}, segmentGroups size: ${segmentGroups.size}`);

                // Check if all frames collected for all segments
                if (
                [...segmentGroups.values()].every(frames => frames.length === stage.animatedFrames.length) &&
                segmentGroups.size === totalSegmentsExpected
                ) {
                    console.log("[ZIP] Condition met! Generating sequences...");

                    segmentGroups.forEach((frames, index) => {
                        const seqName = `${actualName}_ts_${stage.threshold}_${index}`;
                        FullImageYml += generateSequenceImage(seqName, frames) + "\n\n";
                    });

                    const images = [...segmentGroups.keys()].map(i => ({
                        name: `${actualName}_ts_${stage.threshold}_${i}`,
                        x: (i * 256) * SCALE_FACTOR,
                        y: 0
                    }));

                    stageMap.set(`${actualName}_stage_${stage.threshold}`, {
                        threshold: stage.threshold,
                        animated: true,
                        animConfig: stage.animConfig,
                        animatedFrames: stage.animatedFrames,
                        images
                    });

                    console.log("[ZIP] Set animated stage in stageMap for threshold", stage.threshold);
                }
            }
        }
    }
    const defaultStage = stageMap.get(`${actualName}_default`);
    let stageThreasholds = [{ tre: 1 }];
    let stagePercentageThreasholds = [{ tre: 100 }];
    const processDefaultOverlaySlices = async (imageData, sidePrefix, offsetX, offsetY, listenerSide, defaultStage) => {
        const slices = await splitLargeImage(imageData, SLICE_WIDTH);

        const topThreshold = stageThreasholds[0]?.tre ?? 1;
        const nextThreshold = stageThreasholds[1]?.tre ?? 0;
        const totalThresholdRange = topThreshold - nextThreshold;

        const percentPerSlice = totalThresholdRange / slices.length;

        slices.forEach((blob, idx) => {
            const suffix = idx === 0 ? '' : `_part${idx}`;
            const imageId = `${actualName}_${sidePrefix}${suffix}`;
            const fileName = `${imageId}.png`;

            assetsDir.file(fileName, blob);
            console.log(`[ZIP] Added overlay ${sidePrefix} part:`, fileName);

            const sliceEnd = topThreshold - ((idx + 1) * percentPerSlice);

            const listener = {
                clazz: 'placeholder',
                value: `(number)papi:papimaths_(clamp(((papi:bossbar_${bossName.value}_current_health / papi:bossbar_${bossName.value}_max_health) - ${sliceEnd}), 0, ${percentPerSlice}) / ${percentPerSlice})`,
                max: (idx * percentPerSlice) !== 0
                ? `(number)papi:papimaths_(papi:bossbar_${bossName.value}_max_health * ${idx * percentPerSlice})`
                : `(number)papi:bossbar_${bossName.value}_max_health`
            };

            FullImageYml += generateListenerImage(
                imageId,
                `betterbossbars/${fileName}`,
                splitsAmount,
                listenerSide,
                listener
            ) + '\n\n';

            if (defaultStage) {
                defaultStage.images.push({
                    name: imageId,
                    x: (parseFloat(offsetX) + (SLICE_WIDTH * idx)) * SCALE_FACTOR,
                    y: parseFloat(offsetY) * SCALE_FACTOR
                });
            }
        });
    };
    // Process overlay images based on bossbarType.
    if (bossbarTypeSelect.value === 'normal') {
        overlayStagesNormal.sort((a, b) => a.threshold - b.threshold);
        overlayStagesNormal.forEach((stage, index) => {
            stageThreasholds.push(
                { tre: stage.treashold }
            );
            stagePercentageThreasholds.push(
                { tre: stage.percentage_tre }
            );
        });
        stageThreasholds.sort((a, b) => a.tre - b.tre);
        console.log("[ZIP] Processing overlay for normal mode...");
        const defaultOverlayData = overlayImage.dataset.default || overlayImage.src;
        if (defaultOverlayData) {
            await processDefaultOverlaySlices(
                defaultOverlayData,
                "mo",
                offsetXInput.value,
                offsetYInput.value,
                reversedCheckbox.checked ? "right" : "left",
                defaultStage
            );
        }
        for (let index = 0; index < overlayStagesNormal.length; index++) {
            const stage = overlayStagesNormal[index];
            const slicedStage = await splitLargeImage(stage.src, 256);
            if (nextStageThreashold < stage.threshold) nextStageThreashold = stage.threshold;
            slicedStage.forEach((blob, sliceIdx) => {
                const suffix = `_os_${index}_${stage.threshold}${sliceIdx > 0 ? `_part${sliceIdx}` : ''}`;
                assetsDir.file(`${actualName}${suffix}.png`, blob);
                console.log("[ZIP] Added overlay stage part (normal):", `${actualName}${suffix}.png`);
                FullImageYml += generateListenerImage(
                    `${actualName}${suffix}`,
                    `betterbossbars/${actualName}${suffix}.png`,
                    splitsAmount,
                    reversedCheckbox.checked ? 'right' : 'left',
                    bossListener,
                    stagePercentageThreasholds[index + 1]?.tre ,
                    stagePercentageThreasholds[index + 2]?.tre || 0
                ) + '\n\n';

                if (stageEntry) {
                    stageEntry.images.push({
                        name: `${actualName}${suffix}`,
                        x: parseFloat(offsetXInput.value) * SCALE_FACTOR,
                        y: parseFloat(offsetYInput.value) * SCALE_FACTOR
                    });
                }
            });
        }
    }
    else {
        overlayStagesDouble.sort((a, b) => a.threshold - b.threshold);
        overlayStagesDouble.forEach((stage, index) => {
            stageThreasholds.push(
                { tre: stage.treashold }
            );
            stagePercentageThreasholds.push(
                { tre: stage.percentage_tre }
            );
        });
        stageThreasholds.sort((a, b) => a.tre - b.tre);
        console.log("[ZIP] Processing overlay for double mode...");
        const defaultOverlayLeft = overlayImageLeft.dataset.default || overlayImageLeft.src;
        const defaultOverlayRight = overlayImageRight.dataset.default || overlayImageRight.src;
        if (defaultOverlayLeft) {
            await processDefaultOverlaySlices(
                defaultOverlayLeft,
                "mol",
                offsetLeftXInput.value,
                offsetLeftYInput.value,
                reversedCheckbox.checked ? "right" : "left",
                defaultStage
            );
        }
        if (defaultOverlayRight) {
            await processDefaultOverlaySlices(
                defaultOverlayRight,
                "mor",
                offsetRightXInput.value,
                offsetRightYInput.value,
                reversedCheckbox.checked ? "left" : "right",
                defaultStage
            );
        }
        for (const [index, stage] of overlayStagesDouble.entries()) {
            const threshold = stage.threshold;
            if (nextStageThreashold < threshold) nextStageThreashold = threshold;
            await processDefaultOverlaySlices(
                stage.left,
                `stl_${threshold}`,
                offsetLeftXInput.value,
                offsetLeftYInput.value,
                reversedCheckbox.checked ? "right" : "left",
                stageMap.get(`${actualName}_stage_${threshold}`)
            )
            await processDefaultOverlaySlices(
                stage.right,
                `str_${threshold}`,
                offsetRightXInput.value,
                offsetRightYInput.value,
                reversedCheckbox.checked ? "left" : "right",
                stageMap.get(`${actualName}_stage_${threshold}`)
            )
        }
    }
    let ymlNamePrefix = "";
    if (document.getElementById("useNameForYmlCheckbox").checked) ymlNamePrefix = `${actualName.toLowerCase()}`;
    else ymlNamePrefix = "better";
    //console.log("Stage images for", stageName, images.map(img => img.name));

    imagesDir.file(ymlNamePrefix + "-bossbar-images.yml", FullImageYml);
    const FullLayoutYml = generateLayoutYamlFromMap(stageMap, bossName.value);
    layoutDir.file(ymlNamePrefix + "-bossbar-layouts.yml", FullLayoutYml);

    const FullPOPUP = generatePopupYaml(
        (document.getElementById('optionalPopupName').value != ""
        &&
        String(document.getElementById('optionalPopupName').value).trim.length === 0
        ) ?
        document.getElementById('optionalPopupName').value : actualName, bossName.value, layoutMap
    );
    popupDir.file(ymlNamePrefix + "-bossbar-popup.yml", FullPOPUP);

    zip.generateAsync({ type: "blob" }).then(function (content) {
        const a = document.createElement("a");
        a.href = URL.createObjectURL(content);
        a.download = actualName + "-bossbar.zip";
        a.click();
    });
}
async function splitLargeImage(base64, maxWidth) {
    const img = new Image();
    img.src = base64;
    await img.decode();

    const parts = [];
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    for (let x = 0; x < img.width; x += maxWidth) {
        const width = Math.min(maxWidth, img.width - x);
        canvas.width = width;    // resetting canvas size clears context
        canvas.height = img.height;

        // Reset context after resizing canvas
        ctx.clearRect(0, 0, width, img.height);

        ctx.drawImage(img, x, 0, width, img.height, 0, 0, width, img.height);

        const blob = await new Promise(res => canvas.toBlob(res));
        parts.push(blob);
    }
    return parts;
}
function generateSingleImage(imageID, imageName) {
    return `${imageID}:
  type: single
  file: ${imageName}`;
}
function generateListenerImage(imageID, imageName, splits, splitType, listener, stagePercent = 100, nextStagePercent = 50) {
    let yaml = `${imageID}:
  type: listener
  file: ${imageName}
  split: ${splits}
  split-type: ${splitType}
  setting:  
    listener:
      class: ${listener.clazz}\n`;
    if (listener.value != null) {
        if (resetHealthBarOnNewStage.checked) {
            yaml += `      value: "(number)papi:papimaths_(((papi:bossbar_Unknown_current_health / papi:bossbar_Unknown_max_health) - ${nextStagePercent}) / (${stagePercent} - ${nextStagePercent}))"\n`;
        } else {
            yaml += `      value: "${listener.value}"\n`;
        }
    }
    if (listener.max != null) {
        if (resetHealthBarOnNewStage.checked) {
            yaml += `      max: "(number)1"\n`;
        } else {
            yaml += `      max: "${listener.max}"\n`;
        }
    }
    return yaml;
}
function generateLayoutYaml(stageName, images, actualName, threshold, isAnimated, animConfig) {
    let yaml = `${stageName}:\n  images:\n`;
    images.forEach((img, index) => {
        yaml += `    ${index + 1}:\n`;
        yaml += `      name: ${img.name}\n`;
        yaml += `      x: ${img.x - halfWidth}\n`;
        yaml += `      y: ${img.y}\n`;
        if (SCALE_FACTOR != 1)
        yaml += `      scale: ${SCALE_FACTOR}\n`;
    });
    if (isAnimated && animConfig) {
        yaml += `  animations:\n`;
        yaml += `    type: ${animConfig.type}\n`;
        yaml += `    duration: ${animConfig.duration}\n`;
        if (animConfig.xEquation && animConfig.xEquation != 0)
        yaml += `    x-equation: ${animConfig.xEquation}\n`;
        if (animConfig.yEquation && animConfig.yEquation != 0)
        yaml += `    y-equation: ${animConfig.yEquation}\n`;
    }
    yaml += `  conditions:\n`;
    yaml += `    1:\n`;
    yaml += `      first: (number)papi:bossbar_${actualName}_stage\n`;
    yaml += `      second: ${threshold}\n`;
    yaml += `      operation: "=="\n`;
    return yaml;
}
function generateLayoutYamlFromMap(stageMapB, actualName) {
    let yaml = '';
    let index = 0;
    for (const [stageName, stageData] of stageMapB.entries()) {
        yaml += generateLayoutYaml(stageName, stageData.images, actualName, stageData.threshold, stageData.animated, stageData.animConfig);
        yaml += '\n';
        layoutMap.set(stageName, index);
        index++;
    }
    return yaml;
}
function generateSequenceImage(imageID, framesArray) {
    framesArray.sort((a, b) => a.order - b.order);
    let yaml = `${imageID}:\n  type: sequence\n  files:\n`;
    framesArray.forEach((frame) => {
        if (frame.duration && frame.duration !== 1) {
            yaml += `    - betterbossbars/${frame.fileName}:${frame.duration}\n`;
        } else {
            yaml += `    - betterbossbars/${frame.fileName}\n`;
        }
    });
    return yaml;
}
function generatePopupYaml(actualName, usedName, layouts) {
    let yaml = `${actualName}:\n  group: betterbossbars\n  unique: true\n  always-check-condition: false\n  layouts:\n`;
    layouts.forEach((index, layout) => {
        yaml += `    ${index + 1}:\n`;
        yaml += `      name: ${layout}\n`;
        yaml += `      x: 50\n`;
        yaml += `      y: 0\n`;
    });
    yaml += `  conditions:\n`;
    yaml += `    1:\n`;
    yaml += `      first: (boolean)papi:bossbar_${usedName}_visible\n`;
    yaml += `      second: "true"\n`;
    yaml += `      operator: '=='\n`;
    return yaml;
}
function updateOverlayCrop() {
    const healthPercent = healthSlider.value / 100;
    let displayHealth = healthPercent;

    // If resetHealthBarOnNewStage is enabled, normalize progress within the current stage.
    if (resetHealthBarOnNewStage.checked) {
        // Sort thresholds descending (e.g. [0.9, 0.7, 0.5, 0.33])
        const sortedThresholds = stageIntervals.slice().sort((a, b) => b - a);
        // Build boundaries: full health (1), followed by the sorted thresholds, then 0.
        const boundaries = [1, ...sortedThresholds, 0];

        // Find the stage in which healthPercent falls.
        for (let i = 0; i < boundaries.length - 1; i++) {
            // We check if healthPercent is between boundaries[i] (upper) and boundaries[i+1] (lower)
            if (healthPercent <= boundaries[i] && healthPercent > boundaries[i + 1]) {
                // Normalize the health progress within this stage.
                displayHealth = (boundaries[i] - healthPercent) / (boundaries[i] - boundaries[i + 1]);
                break;
            }
        }
    }

    const containerWidth = bossbarDisplay.clientWidth;
    const scaleValue =
    parseFloat(getComputedStyle(bossbarDisplay).getPropertyValue('--scale')) || 1;

    if (bossbarTypeSelect.value === 'normal') {
        const posX = parseFloat(offsetXInput.value) || 0;
        const posY = parseFloat(offsetYInput.value) || 0;

        overlayImage.style.transform = `translate(${posX * scaleValue}px, ${posY * scaleValue}px) scale(${scaleValue})`;
        const visibleWidth = containerWidth * displayHealth;
        const cropPx = containerWidth - visibleWidth;
        if (!reversedCheckbox.checked) {
            overlayImage.style.clipPath = `inset(0 ${cropPx}px 0 0)`;
        } else {
            overlayImage.style.clipPath = `inset(0 0 0 ${cropPx}px)`;
        }
        overlayImage.style.display = 'block';
        overlayImageLeft.style.display = 'none';
        overlayImageRight.style.display = 'none';
    } else if (bossbarTypeSelect.value === 'double') {
        const halfWidth = containerWidth / 2;
        const leftPosX = parseFloat(offsetLeftXInput.value) || 0;
        const leftPosY = parseFloat(offsetLeftYInput.value) || 0;
        overlayImageLeft.style.transform = `translate(${leftPosX * scaleValue}px, ${leftPosY * scaleValue}px) scale(${scaleValue})`;
        const rightPosX = parseFloat(offsetRightXInput.value) || 0;
        const rightPosY = parseFloat(offsetRightYInput.value) || 0;
        overlayImageRight.style.transform = `translate(${rightPosX * scaleValue}px, ${rightPosY * scaleValue}px) scale(${scaleValue})`;
        const visibleHalf = halfWidth * displayHealth;
        const cropHalf = halfWidth - visibleHalf;
        if (!reversedCheckbox.checked) {
            overlayImageLeft.style.clipPath = `inset(0 ${cropHalf}px 0 0)`;
            overlayImageRight.style.clipPath = `inset(0 0 0 ${cropHalf}px)`;
        } else {
            overlayImageLeft.style.clipPath = `inset(0 0 0 ${cropHalf}px)`;
            overlayImageRight.style.clipPath = `inset(0 ${cropHalf}px 0 0)`;
        }
        overlayImageLeft.style.display = 'block';
        overlayImageRight.style.display = 'block';
        overlayImage.style.display = 'none';
    }
}
function updateScale() {
    const containerWidth = bossbarDisplay.clientWidth;
    const containerHeight = bossbarDisplay.clientHeight;
    const images = [];
    if (textureImage.src) images.push(textureImage);
    if (overlayImage.src) images.push(overlayImage);
    if (overlayImageLeft.src) images.push(overlayImageLeft);
    if (overlayImageRight.src) images.push(overlayImageRight);
    if (images.length === 0) return;
    let largest = images[0];
    let largestArea = largest.naturalWidth * largest.naturalHeight;
    images.forEach((img) => {
        const area = img.naturalWidth * img.naturalHeight;
        if (area > largestArea) {
            largest = img;
            largestArea = area;
        }
    });
    let scaleFactor = 1;
    if (largest.naturalWidth > containerWidth || largest.naturalHeight > containerHeight) {
        scaleFactor = Math.min(containerWidth / largest.naturalWidth, containerHeight / largest.naturalHeight);
    }
    bossbarDisplay.style.setProperty('--scale', scaleFactor);
}
function finalizeStageEdit() {
    textureStages.sort((a, b) => b.threshold - a.threshold);
    renderTextureStageList();
    updateStageImages();
    document.getElementById('editStageModal').classList.add('hidden');
};
function loadLocalImage(fileInput, imgElement, callback) {
    if (fileInput.files && fileInput.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            imgElement.src = e.target.result;

            if (!imgElement.dataset.default) {
                imgElement.dataset.default = e.target.result;
            }
            if (callback) callback();
        };
        reader.readAsDataURL(fileInput.files[0]);
    }
}
function renderTextureStageList() {
    console.log("Current texture stages:", textureStages);
    if (bossbarTypeSelect.value === 'double')
    console.log("Current overlay stages (double):", overlayStagesDouble);
    else
    console.log("Current overlay stages (normal):", overlayStagesNormal);

    const tableBody = document.querySelector("#textureStageTable tbody");
    tableBody.innerHTML = "";
    textureStages.forEach((stage, index) => {
        const row = document.createElement("tr");

        // Display threshold.
        const thresholdCell = document.createElement("td");
        thresholdCell.textContent = stage.threshold;
        row.appendChild(thresholdCell);

        // Display preview image.
        const previewCell = document.createElement("td");
        const previewImg = document.createElement("img");
        if (stage.animated && stage.animatedFrames && stage.animatedFrames.length > 0) {
            // Use first frame's source for preview.
            previewImg.src = stage.animatedFrames[0].src;
        } else {
            previewImg.src = stage.src;
        }
        previewImg.style.maxWidth = "100px";
        previewImg.style.maxHeight = "50px";
        previewCell.appendChild(previewImg);
        row.appendChild(previewCell);

        // Action cell (Edit button).
        const actionCell = document.createElement("td");
        const editButton = document.createElement("button");
        editButton.textContent = "Edit";
        editButton.addEventListener("click", () => openEditModal(index));
        actionCell.appendChild(editButton);
        row.appendChild(actionCell);

        tableBody.appendChild(row);
    });
}
function playAnimation(config) {
    const startTime = performance.now();
    let running = true;
    let frameId; // Store the ID of the requestAnimationFrame

    function step() {
        if (!running) {
            cancelAnimationFrame(frameId);
            return;
        }
        const now = performance.now();
        let t = ((now - startTime) / 1000) * 60;
        if (t > config.duration) {
            if (config.type === 'play_once') {
                t = config.duration;
            } else if (config.type === 'loop') {
                t = t % config.duration;
            }
        }
        const scope = { t: t, pi: Math.PI };
        let xOffset, yOffset;
        try {
            xOffset = math.evaluate(config['x-equation'], scope);
            yOffset = math.evaluate(config['y-equation'], scope);
        } catch (e) {
            console.error('Error evaluating equation:', e);
            xOffset = 0;
            yOffset = 0;
        }

        // Update the transforms for all relevant elements.
        textureImage.style.transform = `translate(${xOffset}px, ${yOffset}px) scale(${getScale()})`;
        animatedImage.style.transform = `translate(${xOffset}px, ${yOffset}px) scale(${getScale()})`;
        overlayImage.style.transform = `translate(${offsetXInput.value * getScale() + xOffset}px, ${offsetYInput.value * getScale() + yOffset}px) scale(${getScale()})`;
        overlayImageLeft.style.transform = `translate(${offsetLeftXInput.value * getScale() + xOffset}px, ${offsetLeftYInput.value * getScale() + yOffset}px) scale(${getScale()})`;
        overlayImageRight.style.transform = `translate(${offsetRightXInput.value * getScale() + xOffset}px, ${offsetRightYInput.value * getScale() + yOffset}px) scale(${getScale()})`;

        if (t < config.duration || config.type === 'loop') {
            frameId = requestAnimationFrame(step);
        }
    }

    frameId = requestAnimationFrame(step);

    return {
        stop: function () {
            running = false;
            cancelAnimationFrame(frameId);
        }
    };
}
function getScale() {
    return parseFloat(getComputedStyle(bossbarDisplay).getPropertyValue('--scale')) || 1;
}
function animateImage(frames, imageElement, tickDurationMs = 50) {
    let currentFrame = 0;
    let running = true;

    function updateFrame() {
        if (!running) return;
        imageElement.src = frames[currentFrame].src;
        const delay = frames[currentFrame].duration * tickDurationMs;
        currentFrame = (currentFrame + 1) % frames.length;
        setTimeout(updateFrame, delay);
    }

    updateFrame();

    return {
        stop: function () {
            running = false;
        }
    };
}
function readFileAsDataURL(file, shouldLog = true) {
    return new Promise((resolve, reject) => {
        if (!file) {
            if (shouldLog) console.error("[readFileAsDataURL] No file provided.");
            return reject(new Error("No file provided"));
        }
        const reader = new FileReader();
        reader.onload = (e) => {
            if (shouldLog) console.log("[readFileAsDataURL] Loaded file:", file.name);
            resolve(e.target.result);
        };
        reader.onerror = (err) => {
            if (shouldLog) console.error("[readFileAsDataURL] Error reading file:", file.name, err);
            reject(err);
        };
        reader.readAsDataURL(file);
    });
}
function getNextStage(stagesArray, currentHealth) {
    let activeStage = null;

    // Iterate over all stages in the map.
    for (const stage of stagesArray.values()) {
        if (stage.threshold > currentHealth) {
            if (!activeStage || stage.threshold < activeStage.threshold) {
                activeStage = stage;
            }
        }
    }
    if (activeStage == null) {
        if (document.getElementById('animatedCheckbox').checked) {
            return defaultMainTextureAnimation;
        }
        else {
            return { src: document.getElementById('textureStageFile').files[0] };
        }
    }

    return activeStage;
}
function getNextStageOverlay(stagesArray, currentHealth) {
    let activeStage = null;
    // Iterate over the array to see if any stage has a threshold > currentHealth.
    for (let i = 0; i < stagesArray.length; i++) {
        let stage = stagesArray[i];
        if (stage.threshold > currentHealth) {
            if (!activeStage || stage.threshold < activeStage.threshold) {
                activeStage = stage;
            }
        }
    }
    // If no stage found, use pre-stored default overlay.
    if (!activeStage) {
        if (bossbarTypeSelect.value === 'double') {
            activeStage = defaultOverlayDouble;
        } else {
            activeStage = defaultOverlayNormal;
        }
    }
    return activeStage;
}
function updateStageImages() {
    const healthPercent = parseFloat(healthSlider.value);

    // Retrieve the active texture stage (synchronously from an array or similar)
    let activeTextureStage = getNextStage(textureStages, healthPercent);
    if (!activeTextureStage) {
        console.warn("No active texture stage found.");
        return;
    }
    console.log("Active texture stage:", activeTextureStage);

    if (bossbarTypeSelect.value === 'double') {
        let stageOverlay = getNextStageOverlay(overlayStagesDouble, healthPercent);
        console.log("Active overlay stage (double):", stageOverlay);
        // Update texture image
        if (activeTextureStage.animated && activeTextureStage.animatedFrames && activeTextureStage.animatedFrames.length > 0) {
            if (animationHandler != null) animationHandler.stop();
            animationHandler = animateImage(activeTextureStage.animatedFrames, animatedImageEl);
        }
        else {
            const textureSrc = activeTextureStage.src || (textureImage.dataset.default || textureImage.src);
            if (textureSrc) textureImage.src = textureSrc;
        }
        // Update overlay images for double mode.
        if (stageOverlay) {
            overlayImageLeft.src = stageOverlay.left;
            overlayImageRight.src = stageOverlay.right;
            overlayImage.style.display = "none";
            overlayImageLeft.style.display = "block";
            overlayImageRight.style.display = "block";
        }
    }
    else {
        // Single mode.
        let stageOverlay = getNextStageOverlay(overlayStagesNormal, healthPercent);
        console.log("Active overlay stage (single):", stageOverlay);
        if (activeTextureStage.animated && activeTextureStage.animatedFrames && activeTextureStage.animatedFrames.length > 0) {
            if (animationHandler != null) animationHandler.stop();
            animationHandler = animateImage(activeTextureStage.animatedFrames, animatedImageEl);
        }
        else {
            const textureSrc = activeTextureStage.src || (textureImage.dataset.default || textureImage.src);
            if (textureSrc) textureImage.src = textureSrc;
        }
        if (stageOverlay) {
            const overlaySrc = stageOverlay.src || (overlayImage.dataset.default || overlayImage.src);
            overlayImage.src = overlaySrc;
            overlayImage.style.display = "block";
            overlayImageLeft.style.display = "none";
            overlayImageRight.style.display = "none";
        }
    }
}