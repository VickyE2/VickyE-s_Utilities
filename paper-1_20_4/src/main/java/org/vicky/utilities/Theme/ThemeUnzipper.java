/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.Theme;

import static org.vicky.global.Global.hookedPlugins;
import static org.vicky.global.Global.storer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ConfigManager;
import org.vicky.utilities.ContextLogger.ContextLogger;

/**
 * A utility class to manage and process themes.
 */
public class ThemeUnzipper {
	private final JavaPlugin plugin;
	private final ConfigManager guiManager;
	private final ConfigManager buttonManager;
	private final ConfigManager iconsManager;
	public final List<String> requiredGuis = new ArrayList<>();
	public final List<String> requiredButtons = new ArrayList<>();
	public final List<String> requiredAnimatedImages = new ArrayList<>();
	public Map<String, Integer> buttonVerticalSplits = new HashMap<>();
	private final String[] allowedImageFormats = {".png", ".jpeg", ".jpg"};

	ContextLogger logger = new ContextLogger(ContextLogger.ContextType.FEATURE, "THEME-UNZIPPER");

	public ThemeUnzipper(JavaPlugin plugin) {
		this.plugin = plugin;
		File configFile = new File(plugin.getDataFolder().getParentFile().getAbsolutePath(),
				"/ItemsAdder/contents/vicky_themes/configs/");

		if (!configFile.exists()) {
			configFile.mkdir();
		}

		try {
			Thread.sleep(700);
			this.guiManager = new ConfigManager(false);
			guiManager.createPathedConfig(configFile + "/guis.yml");
			guiManager.loadConfigValues();
			guiManager.setConfigValue("info", "namespace", "vicky_themes", null);

			this.iconsManager = new ConfigManager(false);
			iconsManager.createPathedConfig(configFile + "/icons.yml");
			iconsManager.loadConfigValues();
			iconsManager.setConfigValue("info", "namespace", "vicky_themes", null);

			this.buttonManager = new ConfigManager(false);
			buttonManager.createPathedConfig(configFile + "/items.yml");
			buttonManager.loadConfigValues();
			buttonManager.setConfigValue("info", "namespace", "vicky_themes", null);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Path> getAllZipFiles(String directoryPath) throws IOException {
		Path dirPath = Paths.get(directoryPath);

		// Ensure the directory exists
		if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
			throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
		}

		// Collect all ZIP files in the directory
		return Files.walk(dirPath).filter(path -> path.toString().toLowerCase().endsWith(".zip"))
				.collect(Collectors.toList());
	}

	public void setRequiredImages() {
		requiredAnimatedImages.clear();
		requiredButtons.clear();
		requiredGuis.clear();

		if (hookedPlugins.stream().anyMatch(k -> k.getName().equals("VickyEs-Party_and_Friends"))) {
			requiredGuis.addAll(List.of("friends_gui_change_status_panel", "friends_gui_f_profile_panel",
					"friends_gui_main_panel", "friends_gui_message_panel", "friends_gui_message_request_panel",
					"friends_gui_request_panel", "party_gui_main_panel", "party_gui_member_panel",
					"friends_gui_status_change_panel", "friends_gui_change_color_panel", "friends_gui_settings_panel"));
		}
		if (hookedPlugins.stream().anyMatch(k -> k.getName().equals("VickyEs_Survival_Plus_Essentials"))) {
			requiredGuis.add("trinket_gui");
		}

		requiredGuis.addAll(List.of("seven_by_four", "seven_by_one_lower", "seven_by_one_top", "seven_by_one_by_three",
				"seven_by_one_centered", "seven_by_three_lower", "seven_by_three_top", "seven_by_two_center",
				"seven_by_two_lower", "seven_by_two_top", "five_by_six_left", "five_by_six_right", "full_grid",
				"anvil"));

		requiredButtons.addAll(Arrays.asList("accept_button_long", "accept_button_small", "add_favourite_button",
				"back_down_button", "back_up_button", "cancel_button_small", "demote_button", "friend_request_empty",
				"friend_request_has", "invite_button", "jump_button", "kick_long_button", "leave_button", "left_arrow",
				"message_request_empty", "message_request_has", "party_long_button", "promote_button",
				"reject_button_long", "remove_favourite_button", "right_arrow", "settings_rounded_left",
				"settings_rounded", "slider_0", "slider_1", "slider_2", "slider_3", "sort", "trashcan_button",
				"chat_box"));

		requiredAnimatedImages.add("loading_gif");

		buttonVerticalSplits.put("accept_button_long", 3);
		buttonVerticalSplits.put("accept_button_small", 0);
		buttonVerticalSplits.put("add_favourite_button", 0);
		buttonVerticalSplits.put("back_down_button", 3);
		buttonVerticalSplits.put("back_up_button", 3);
		buttonVerticalSplits.put("cancel_button_small", 0);
		buttonVerticalSplits.put("chat_box", 7);
		buttonVerticalSplits.put("demote_button", 3);
		buttonVerticalSplits.put("friend_request_empty", 0);
		buttonVerticalSplits.put("friend_request_has", 0);
		buttonVerticalSplits.put("invite_button", 3);
		buttonVerticalSplits.put("jump_button", 3);
		buttonVerticalSplits.put("kick_long_button", 2);
		buttonVerticalSplits.put("leave_button", 0);
		buttonVerticalSplits.put("left_arrow", 0);
		buttonVerticalSplits.put("message_request_empty", 0);
		buttonVerticalSplits.put("message_request_has", 0);
		buttonVerticalSplits.put("party_long_button", 2);
		buttonVerticalSplits.put("promote_button", 3);
		buttonVerticalSplits.put("reject_button_long", 3);
		buttonVerticalSplits.put("remove_favourite_button", 0);
		buttonVerticalSplits.put("right_arrow", 0);
		buttonVerticalSplits.put("settings_rounded_left", 0);
		buttonVerticalSplits.put("settings_rounded", 0);
		buttonVerticalSplits.put("slider_0", 0);
		buttonVerticalSplits.put("slider_1", 0);
		buttonVerticalSplits.put("slider_2", 0);
		buttonVerticalSplits.put("slider_3", 0);
		buttonVerticalSplits.put("sort", 0);
		buttonVerticalSplits.put("trashcan_button", 0);
		buttonVerticalSplits.put("loading_gif", 0);
	}

	/**
	 * This method scans through all zip archives in the themes folder of the
	 * plugins data folder, stores them to a global {@link ThemeStorer storer} and
	 * then persists instances of them into a global
	 * {@link org.vicky.utilities.DatabaseManager.templates.Theme DatabaseEntity}
	 *
	 * @throws IOException
	 *             In cases of not being able to access the file or make changes to
	 *             it. This exception is thrown
	 */
	public void downloadThemes() throws IOException {
		setRequiredImages();
		Path themesPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "themes");
		List<Path> zipFiles = getAllZipFiles(themesPath.toString());

		if (!zipFiles.isEmpty()) {
			for (Path zipPath : zipFiles) {
				boolean filenowFound = false;
				boolean fileFound;
				ConfigManager manager = new ConfigManager(false);
				try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
					ZipEntry requiredEntry = zipFile.getEntry("themes.yml");
					if (requiredEntry == null || requiredEntry.isDirectory()) {
						logger.print("Required file, " + requiredEntry + ", not found in the Theme Pack: "
								+ zipFile.getName(), true);
						break;
					}
				}
				manager.loadConfigFromZip(zipPath, "themes.yml");
				try {
					ConfigurationNode themesNode = manager.rootNode.node("themes");
					if (!themesNode.virtual()) {
						for (ConfigurationNode themeNode : themesNode.childrenMap().values()) {
							final Map<String, Integer> heightDiffMap = new HashMap<>();
							String themeName = themeNode.node("theme_name").getString();
							String themeId = themeNode.node("theme_id").getString();
							ConfigurationNode guiFolder = themeNode.node("gui_folder");
							ConfigurationNode description = themeNode.node("description");
							ConfigurationNode buttonsFolder = themeNode.node("buttons_folder");
							ConfigurationNode textsFolder = themeNode.node("texts_folder");
							ConfigurationNode animatedFolder = themeNode.node("guis_folder");
							ConfigurationNode heightDiffsNode = themeNode.node("height_diffs");
							if (!heightDiffsNode.virtual()) {
								for (Map.Entry<Object, ? extends ConfigurationNode> entry : heightDiffsNode
										.childrenMap().entrySet()) {
									String key = String.valueOf(entry.getKey());
									int value = entry.getValue().getInt(0);
									heightDiffMap.put(key, value);
								}
							}
							logger.print("Loading theme with id: " + themeId, ContextLogger.LogType.PENDING);
							if (!storer.isRegisteredTheme(themeId)) {
								if (guiFolder.virtual() || guiFolder.isNull()) {
									logger.print(
											"The theme with id: " + themeId + " fails to provide a gui_folder field",
											true);
									break;
								}
								if (buttonsFolder.virtual() || buttonsFolder.isNull()) {
									logger.print("The theme with id: " + themeId
											+ " fails to provide a buttons_folder field", true);
									break;
								}
								if (textsFolder.virtual() || textsFolder.isNull()) {
									logger.print(
											"The theme with id: " + themeId + " fails to provide a texts_folder field",
											true);
									break;
								}
								if (animatedFolder.virtual() || animatedFolder.isNull()) {
									logger.print(
											"The theme with id: " + themeId + " fails to provide a gifs_folder field",
											true);
									break;
								}
								for (String currentGui : requiredGuis) {
									try (ZipFile zip = new ZipFile(zipPath.toFile())) {
										fileFound = false;
										Path outputFolderPath = Path
												.of(plugin.getDataFolder().getParentFile().getAbsolutePath()
														+ "/ItemsAdder/contents/vicky_themes/textures/gui/" + themeId);
										for (String format : allowedImageFormats) {
											String file = guiFolder.getString() + "/" + currentGui + "_" + themeId
													+ format;
											String imagefile = currentGui + "_" + themeId + format;
											ZipEntry entry = zip.getEntry(file);
											if (entry != null && !entry.isDirectory()) {
												fileFound = true;
												filenowFound = true;
												File outputFile = new File(outputFolderPath.toString(), imagefile);
												File parentDir = outputFile.getParentFile();
												if (!parentDir.exists()) {
													if (!parentDir.mkdirs()) {
														logger.print("Failed to create directories: "
																+ parentDir.getAbsolutePath(), true);
														continue;
													}
												}

												// Save the image to the output folder
												try (InputStream inputStream = zip.getInputStream(entry);
														FileOutputStream fos = new FileOutputStream(outputFile)) {
													byte[] buffer = new byte[1024];
													int bytesRead;
													while ((bytesRead = inputStream.read(buffer)) != -1) {
														fos.write(buffer, 0, bytesRead);
													}

												} catch (IOException e) {
													logger.print("Error saving the image: " + e.getMessage(), true);

													break;
												}

												guiManager.setConfigValue("font_images",
														currentGui + "_" + themeId + ".path",
														"gui/" + themeId + "/" + currentGui + "_" + themeId + format,
														null);
												guiManager.setConfigValue("font_images",
														currentGui + "_" + themeId + ".suggest_in_command", false,
														null);
												guiManager.setConfigValue("font_images",
														currentGui + "_" + themeId + ".y_position",
														heightDiffMap.getOrDefault(currentGui, 0), null);

												break;
											}
										}
										if (!fileFound) {
											filenowFound = false;
											for (String format : allowedImageFormats) {
												logger.print(
														"yellow[ No file found for any allowed format. Using provided value"
																+ " from config if any.]",
														ContextLogger.LogType.WARNING);
												ConfigurationNode guisNode = themeNode.node("guis");
												String guiValue = guisNode.getString(currentGui);
												String file = manager.getStringValue(
														guiFolder.getString() + "/" + guiValue + format);
												String imagefile = currentGui + "_" + themeId + format;
												logger.print("Checking for file: " + file);
												ZipEntry entry = zip.getEntry(file);
												if (entry != null && !entry.isDirectory()) {
													filenowFound = true;
													if (!guisNode.virtual()) {
														File outputFile = new File(outputFolderPath.toString(),
																imagefile);
														File parentDir = outputFile.getParentFile();
														if (!parentDir.exists()) {
															if (parentDir.mkdirs()) {
																logger.print("Created directories: "
																		+ parentDir.getAbsolutePath(), true);
															} else {
																logger.print("Failed to create directories: "
																		+ parentDir.getAbsolutePath(), true);
																continue; // Skip this file if directories cannot be
																			// created
															}
														}

														try (InputStream inputStream = zip.getInputStream(entry);
																FileOutputStream fos = new FileOutputStream(
																		outputFile)) {

															byte[] buffer = new byte[1024];
															int bytesRead;
															while ((bytesRead = inputStream.read(buffer)) != -1) {
																fos.write(buffer, 0, bytesRead);
															}

														} catch (IOException e) {
															logger.print("Error saving the image: " + e.getMessage(),
																	true);
															break;
														}

														Path imagePath = Path.of(outputFile.getPath()); // Replace with
																										// your image
																										// path

														// Load the image
														Image image = Toolkit.getDefaultToolkit()
																.getImage(imagePath.toString());

														// Create a MediaTracker to track the loading of the image
														MediaTracker tracker = new MediaTracker(new Canvas());
														tracker.addImage(image, 0);

														guiManager.setConfigValue("font_images",
																currentGui + "_" + themeId + ".path", "gui/" + themeId
																		+ "/" + currentGui + "_" + themeId + format,
																null);
														guiManager.setConfigValue("font_images",
																currentGui + "_" + themeId + ".suggest_in_command",
																false, null);
														guiManager.setConfigValue("font_images",
																currentGui + "_" + themeId + ".y_position",
																heightDiffMap.getOrDefault(currentGui, 0), null);

														logger.print("file_now_found: " + filenowFound);
														break;
													} else {
														logger.print(ANSIColor.colorize(
																"yellow[ No Guis found for theme: " + themeId + "]"));
														break;
													}
												}
											}
										}
									} catch (IOException e) {
										logger.print("Failed to get zip file: " + e.getMessage(), true);
										e.printStackTrace();
										break;
									}

									if (!filenowFound || !fileFound) {
										logger.print("The theme with id: " + themeId + " fails to provide a gui for '"
												+ currentGui + "_" + themeId + "'", true);
										break;
									}
								}
								for (String currentButton : requiredButtons) {
									try (ZipFile zip = new ZipFile(zipPath.toFile())) {
										fileFound = false;
										filenowFound = false;
										Path outputFolderPath = Path
												.of(plugin.getDataFolder().getParentFile().getAbsolutePath()
														+ "/ItemsAdder/contents/vicky_themes/textures/items/"
														+ themeId);
										for (String format : allowedImageFormats) {
											String file = buttonsFolder.getString() + "/" + currentButton + "_"
													+ themeId + format;
											String outputImage = buttonsFolder.getString() + "/" + currentButton + "_"
													+ themeId + format;
											String unsplitoutputImage = currentButton + "_" + themeId + format;
											ZipEntry entry = zip.getEntry(file);
											if (entry != null && !entry.isDirectory()) {
												fileFound = true;
												filenowFound = true;
												try {
													Files.createDirectories(outputFolderPath);
												} catch (IOException e) {
													logger.print("Failed to create directory: " + outputFolderPath
															+ " - " + e.getMessage(), true);
													return;
												}

												File outputFile = new File(outputFolderPath.toString(), outputImage);
												File parentDir = outputFile.getParentFile();
												if (!parentDir.exists()) {
													if (!parentDir.mkdirs()) {
														logger.print(ANSIColor
																.colorize("yellow[Failed to create directories: ]"
																		+ parentDir.getAbsolutePath()),
																true);
														continue;
													}
												}

												int verticalSplit = buttonVerticalSplits.getOrDefault(currentButton, 0);
												if (verticalSplit == 0) {
													// Save the image to the output folder
													File unsplitoutputFile = new File(outputFolderPath.toString(),
															unsplitoutputImage);
													try (FileOutputStream fos = new FileOutputStream(unsplitoutputFile);
															InputStream inputStream = zip.getInputStream(entry)) {
														byte[] buffer = new byte[1024];
														int bytesRead;
														while ((bytesRead = inputStream.read(buffer)) != -1) {
															fos.write(buffer, 0, bytesRead);
														}
													} catch (IOException e) {
														logger.print(
																ANSIColor.colorize("yellow[Error saving the image: "
																		+ e.getMessage() + "]"));
													}

													buttonManager.setConfigValue("items",
															currentButton + "_" + themeId + ".display_name",
															currentButton, null);
													buttonManager.setConfigValue("items",
															currentButton + "_" + themeId + ".resource.material",
															"PAPER", null);
													List<String> textures = new ArrayList<>();
													textures.add("items/" + themeId + "/" + currentButton + "_"
															+ themeId + format);
													buttonManager.setConfigValue("items",
															currentButton + "_" + themeId + ".resource.generate", true,
															null);
													buttonManager.setListConfigValue("items." + currentButton + "_"
															+ themeId + ".resource.textures", textures);

												} else if (!currentButton.equalsIgnoreCase("chat_box")) {

													try (InputStream inputStream = zip.getInputStream(entry);
															FileOutputStream fos = new FileOutputStream(outputFile)) {

														byte[] buffer = new byte[1024];
														int bytesRead;
														while ((bytesRead = inputStream.read(buffer)) != -1) {
															fos.write(buffer, 0, bytesRead);
														}

													} catch (IOException e) {
														logger.print(
																ANSIColor.colorize("yellow[Error saving the image: "
																		+ e.getMessage() + "]"));
													}

													List<String> images = ImageDivider(null,
															outputFile.getAbsolutePath(), 0, verticalSplit,
															currentButton + "_" + themeId, outputFolderPath.toString(),
															false);
													for (String image : images) {
														buttonManager.setConfigValue("items", image + ".display_name",
																currentButton, null);
														buttonManager.setConfigValue("items",
																image + ".resource.material", "PAPER", null);
														List<String> textures = new ArrayList<>();
														textures.add("items/" + themeId + "/" + image + ".png");
														buttonManager.setConfigValue("items",
																image + ".resource.generate", true, null);
														buttonManager.setListConfigValue(
																"items." + image + ".resource.textures", textures);
													}
												} else {
													List<File> files = new ArrayList<>();
													List<String> divided_images;
													Enumeration<? extends ZipEntry> images = zip.entries();

													try (InputStream inputStream = zip.getInputStream(entry);
															FileOutputStream fos = new FileOutputStream(outputFile)) {

														byte[] buffer = new byte[1024];
														int bytesRead;
														while ((bytesRead = inputStream.read(buffer)) != -1) {
															fos.write(buffer, 0, bytesRead);
														}

													} catch (IOException e) {
														logger.print(
																ANSIColor.colorize("yellow[Error saving the image: "
																		+ e.getMessage() + "]"));
													}

													while (images.hasMoreElements()) {
														ZipEntry entry1 = images.nextElement();
														String filename = entry1.getName();
														if (filename.startsWith(
																Objects.requireNonNull(textsFolder.getString()))
																&& (filename.endsWith(".png")
																		|| filename.endsWith(".jpeg")
																		|| filename.endsWith(".jpg"))) {

															// Save the image to a temp file
															File tempFile = new File(outputFolderPath + "/buttons",
																	new File(filename).getName());
															try (InputStream inputStream = zip.getInputStream(entry1);
																	FileOutputStream fos = new FileOutputStream(
																			tempFile)) {

																byte[] buffer = new byte[1024];
																int bytesRead;
																while ((bytesRead = inputStream.read(buffer)) != -1) {
																	fos.write(buffer, 0, bytesRead);
																}
																files.add(tempFile); // Add the temp file to the list of
																						// files
															} catch (IOException e) {
																logger.print(
																		"Error saving the image: " + e.getMessage(),
																		true);
															}
														}
													}

													// Now process each extracted image file using the ImageDivider
													// method
													for (File currentFile : files) {
														// Call ImageDivider for each file, pass the vertical split
														// amount
														String filename = currentFile.getName().replace(".png", "");
														divided_images = ImageDivider(currentFile.getAbsolutePath(),
																outputFile.getAbsolutePath(), 0, verticalSplit,
																filename, outputFolderPath.toString(), true);
														// Update configuration for each split image
														for (String image : divided_images) {
															// Example of setting the display name, material, and
															// texture
															buttonManager.setConfigValue("items",
																	filename + ".display_name", filename, null);
															buttonManager.setConfigValue("items",
																	filename + ".resource.material", "PAPER", null);

															List<String> textures = new ArrayList<>();
															textures.add("items/" + themeId + "/" + image + ".png");

															// Save the texture list into config
															buttonManager.setConfigValue("items",
																	filename + ".resource.generate", true, null);
															buttonManager.setListConfigValue(
																	"items." + filename + ".resource.textures",
																	textures);
														}
													}
												}
												break;
											}
										}
										if (!fileFound) {
											for (String format : allowedImageFormats) {
												logger.print(
														"No file found for any allowed format. Using provided value from"
																+ " config if any.",
														ContextLogger.LogType.WARNING);
												ConfigurationNode buttonsNode = themeNode.node("buttons");
												String buttonsValue;
												String file = "";
												String imagefile = "";

												if (!buttonsNode.empty()) {
													// Retrieve the correct button node using the key (currentButton)
													ConfigurationNode buttonNode = buttonsNode.node(currentButton);
													// Check if the node exists and has a value
													if (!buttonNode.isNull()) {
														buttonsValue = buttonNode.getString();
														file = buttonsFolder.getString() + "/" + buttonsValue + format;
														imagefile = buttonsValue + "_" + themeId + format;

													} else {
														logger.print("Button node for " + currentButton
																+ " is null or does not exist.", true);
													}
												} else {
													logger.print("Buttons node is empty or does not exist!", true);
												}
												ZipEntry entry = zip.getEntry(file);
												if (entry != null && !entry.isDirectory()) {
													filenowFound = true;
													if (!buttonsNode.virtual()) {
														File outputFile = new File(outputFolderPath.toString(),
																imagefile);
														File parentDir = outputFile.getParentFile();
														if (!parentDir.exists()) {
															if (!parentDir.mkdirs()) {
																logger.print("Failed to create directories: "
																		+ parentDir.getAbsolutePath(), true);
																continue; // Skip this file if directories cannot be
																			// created
															}
														}

														try {
															Files.createDirectories(outputFolderPath); // This will
																										// create all
																										// necessary
																										// parent
															// directories
														} catch (IOException e) {
															logger.print("Failed to create directory: "
																	+ outputFolderPath + " - " + e.getMessage(), true);
															return; // Exit if the directory can't be created
														}

														outputFile = new File(outputFolderPath.toString(), file);
														File unsplitoutputFile = new File(outputFolderPath.toString(),
																imagefile);
														parentDir = outputFile.getParentFile();
														if (!parentDir.exists()) {
															if (!parentDir.mkdirs()) {
																logger.print("Failed to create directories: "
																		+ parentDir.getAbsolutePath(), true);
																continue; // Skip this file if directories cannot be
																			// created
															}
														}

														int verticalSplit = buttonVerticalSplits
																.getOrDefault(currentButton, 0);
														if (verticalSplit == 0) {
															// Save the image to the output folder
															try (InputStream inputStream = zip.getInputStream(entry);
																	FileOutputStream fos = new FileOutputStream(
																			unsplitoutputFile)) {

																byte[] buffer = new byte[1024];
																int bytesRead;
																while ((bytesRead = inputStream.read(buffer)) != -1) {
																	fos.write(buffer, 0, bytesRead);
																}

															} catch (IOException e) {
																logger.print(
																		"Error saving the image: " + e.getMessage(),
																		true);
															}

															buttonManager.setConfigValue("items",
																	currentButton + "_" + themeId + ".display_name",
																	currentButton, null);
															buttonManager.setConfigValue("items", currentButton + "_"
																	+ themeId + ".resource.material", "PAPER", null);
															List<String> textures = new ArrayList<>();
															textures.add("items/" + themeId + "/" + currentButton + "_"
																	+ themeId + format);
															buttonManager.setConfigValue("items", currentButton + "_"
																	+ themeId + ".resource.generate", true, null);
															buttonManager.setListConfigValue("items." + currentButton
																	+ "_" + themeId + ".resource.textures", textures);

														} else if (!currentButton.equalsIgnoreCase("chat_box")) {

															try (InputStream inputStream = zip.getInputStream(entry);
																	FileOutputStream fos = new FileOutputStream(
																			outputFile)) {

																byte[] buffer = new byte[1024];
																int bytesRead;
																while ((bytesRead = inputStream.read(buffer)) != -1) {
																	fos.write(buffer, 0, bytesRead);
																}

															} catch (IOException e) {
																logger.print(
																		"Error saving the image: " + e.getMessage(),
																		true);
															}

															List<String> images = ImageDivider(null,
																	outputFile.getAbsolutePath(), 0, verticalSplit,
																	currentButton + "_" + themeId,
																	outputFolderPath.toString(), false);
															for (String image : images) {
																buttonManager.setConfigValue("items",
																		image + ".display_name", currentButton, null);
																buttonManager.setConfigValue("items",
																		image + ".resource.material", "PAPER", null);
																List<String> textures = new ArrayList<>();
																textures.add("items/" + themeId + "/" + image + ".png");
																buttonManager.setConfigValue("items",
																		image + ".resource.generate", true, null);
																buttonManager.setListConfigValue(
																		"items." + image + ".resource.textures",
																		textures);
															}
														} else {
															List<File> files = new ArrayList<>();
															List<String> divided_images;
															Enumeration<? extends ZipEntry> images = zip.entries();

															try (InputStream inputStream = zip.getInputStream(entry);
																	FileOutputStream fos = new FileOutputStream(
																			outputFile)) {

																byte[] buffer = new byte[1024];
																int bytesRead;
																while ((bytesRead = inputStream.read(buffer)) != -1) {
																	fos.write(buffer, 0, bytesRead);
																}
															} catch (IOException e) {
																logger.print(
																		"Error saving the image: " + e.getMessage(),
																		true);
															}

															while (images.hasMoreElements()) {
																ZipEntry entry1 = images.nextElement();
																String filename = entry1.getName();
																if (filename.startsWith(
																		Objects.requireNonNull(textsFolder.getString()))
																		&& (filename.endsWith(".png")
																				|| filename.endsWith(".jpeg")
																				|| filename.endsWith(".jpg"))) {

																	// Save the image to a temp file
																	File tempFile = new File(
																			outputFolderPath + "/buttons",
																			new File(filename).getName());
																	try (InputStream inputStream = zip
																			.getInputStream(entry1);
																			FileOutputStream fos = new FileOutputStream(
																					tempFile)) {

																		byte[] buffer = new byte[1024];
																		int bytesRead;
																		while ((bytesRead = inputStream
																				.read(buffer)) != -1) {
																			fos.write(buffer, 0, bytesRead);
																		}
																		files.add(tempFile); // Add the temp file to the
																								// list of files
																	} catch (IOException e) {
																		logger.print("Error saving the image: "
																				+ e.getMessage(), true);
																	}
																}
															}

															// Now process each extracted image file using the
															// ImageDivider method
															for (File currentFile : files) {
																// Call ImageDivider for each file, pass the vertical
																// split amount
																String filename = currentFile.getName().replace(".png",
																		"");
																divided_images = ImageDivider(
																		currentFile.getAbsolutePath(),
																		outputFile.getAbsolutePath(), verticalSplit, 0,
																		filename, outputFolderPath.toString(), true);

																// Update configuration for each split image
																for (String image : divided_images) {
																	// Example of setting the display name, material,
																	// and texture
																	buttonManager.setConfigValue("items",
																			filename + ".display_name", filename, null);
																	buttonManager.setConfigValue("items",
																			filename + ".resource.material", "PAPER",
																			null);

																	List<String> textures = new ArrayList<>();
																	textures.add(
																			"items/" + themeId + "/" + image + ".png");

																	// Save the texture list into config
																	buttonManager.setConfigValue("items",
																			filename + ".resource.generate", true,
																			null);
																	buttonManager.setListConfigValue(
																			"items." + filename + ".resource.textures",
																			textures);
																}
															}
														}
													}
												}
											}
										}
									} catch (IOException e) {
										logger.print("Failed to get zip file: " + e.getMessage(), true);
										e.printStackTrace();
										break;
									}

									if (!filenowFound) {
										logger.print(
												"The theme with id: " + themeId + " fails to provide a button for '"
														+ currentButton + "_" + themeId + "'",
												true);
										break;
									}
								}
								for (String animated : requiredAnimatedImages) {
									try (ZipFile zip = new ZipFile(zipPath.toFile())) {
										Path animatedFolderPath = Path
												.of(plugin.getDataFolder().getParentFile().getAbsolutePath()
														+ "/ItemsAdder/contents/vicky_themes/textures/animated/"
														+ themeId);
										for (String format : allowedImageFormats) {
											String animatedFileString = animatedFolder.getString() + "/" + animated
													+ "_" + themeId + format;
											String animatedMcmetaFileString = animatedFolder.getString() + "/"
													+ animated + "_" + themeId + format;
											String imageFile = animated + "_" + themeId + format;
											String imageFileMcmeta = imageFile + ".mcmeta";
											ZipEntry animatedEntry = zip.getEntry(animatedFileString);
											ZipEntry animatedMcmetaEntry = zip.getEntry(animatedMcmetaFileString);
											if (animatedEntry != null && !animatedEntry.isDirectory()) {
												File animatedFile = new File(animatedFolderPath.toString(), imageFile);
												File animatedMcmetaFile = new File(animatedFolderPath.toString(),
														imageFileMcmeta);
												if (animatedMcmetaEntry == null) {
													logger.print(
															ANSIColor.colorize("yellow[Warning handling file "
																	+ animated
																	+ ": No mcmeta for animated image file found... ]"),
															ContextLogger.LogType.WARNING);
												} else {
													InputStream animatedStream = zip.getInputStream(animatedEntry);
													try (FileOutputStream as = new FileOutputStream(animatedFile)) {
														byte[] buffer = new byte[1024];
														int bytesRead;
														while ((bytesRead = animatedStream.read(buffer)) != -1) {
															as.write(buffer, 0, bytesRead);
														}
													} catch (IOException e) {
														logger.print(ANSIColor
																.colorize("yellow[Error saving the animated image: "
																		+ e.getMessage() + "]"));
													}
													InputStream animatedMcmetaStream = zip
															.getInputStream(animatedMcmetaEntry);
													try (FileOutputStream ams = new FileOutputStream(
															animatedMcmetaFile)) {
														byte[] buffer = new byte[1024];
														int bytesRead;
														while ((bytesRead = animatedMcmetaStream.read(buffer)) != -1) {
															ams.write(buffer, 0, bytesRead);
														}
													} catch (IOException e) {
														logger.print(ANSIColor
																.colorize("yellow[Error saving the animated image: "
																		+ e.getMessage() + "]"));
													}
												}
											}
										}
									}
								}
								try (ZipFile zip = new ZipFile(zipPath.toFile())) {
									Path outputIconPath = Path
											.of(plugin.getDataFolder().getParentFile().getAbsolutePath()
													+ "/ItemsAdder/contents/vicky_themes/textures/icons");
									ZipEntry entry = zip.getEntry("icons/" + themeId + ".png");
									try {
										Files.createDirectories(outputIconPath);
									} catch (IOException e) {
										logger.print("Failed to create directory: " + outputIconPath + " - "
												+ e.getMessage(), true);
										return; // Exit if the directory can't be created
									}
									if (entry != null) {
										File actualIcon = new File(outputIconPath.toString(), themeId + ".png");
										try (InputStream inputStream = zip.getInputStream(entry)) {
											try (FileOutputStream fos = new FileOutputStream(actualIcon)) {
												byte[] buffer = new byte[1024];
												int bytesRead;
												while ((bytesRead = inputStream.read(buffer)) != -1) {
													fos.write(buffer, 0, bytesRead);
												}
												iconsManager.setConfigValue("items",
														"icon_" + themeId + ".display_name", themeName + " Icon", null);
												iconsManager.setConfigValue("items",
														"icon_" + themeId + ".resource.material", "PAPER", null);
												iconsManager.setConfigValue("items",
														"icon_" + themeId + ".resource.generate", true, null);
												iconsManager.setListConfigValue(
														"items." + "icon_" + themeId + ".resource.textures",
														List.of("icons/" + themeId));
											} catch (IOException e) {
												logger.print("Error saving the icon: " + e.getMessage(), true);
												break;
											}
										} catch (IOException e) {
											logger.print("Error saving the icon: " + e.getMessage(), true);
											break;
										}
									} else {
										logger.print("Theme with id " + themeId + " fails to provide an icon in icon/"
												+ themeId + ".png", ContextLogger.LogType.AMBIENCE);
										break;
									}
								}
								Path directory = Path.of(plugin.getDataFolder().getParentFile().getAbsolutePath()
										+ "/ItemsAdder/contents/vicky_themes/textures/items/" + themeId + "/buttons");
								Files.walk(directory).sorted(Comparator.reverseOrder()).map(Path::toFile)
										.forEach(File::delete);
								logger.print("Theme with id " + ANSIColor.colorize(themeId, ANSIColor.YELLOW_BOLD)
										+ " has successfully been loaded", ContextLogger.LogType.SUCCESS);
								storer.addTheme(themeId, themeName, description);
							} else {
								logger.print("Theme with id " + themeId + " has already been registered",
										ContextLogger.LogType.AMBIENCE);
								break;
							}
						}
					} else {
						logger.print("No themes found in the configuration.", ContextLogger.LogType.WARNING);
					}
				} catch (Exception e) {
					logger.print("Error while loading themes: " + e.getMessage(), true);
					e.printStackTrace();
				}
			}
		}
	}

	public List<String> ImageDivider(String topImageMain, String bottomImageMain, int b_row, int b_col, String mName,
			String o_path, boolean overlay) {
		List<String> images = new ArrayList<>();
		try {
			// Load the bottom layer image
			BufferedImage bottomImage = ImageIO.read(new File(bottomImageMain));
			// Load the top layer image if overlaying
			BufferedImage topImage = overlay ? ImageIO.read(new File(topImageMain)) : null;
			// Create the output directory if it doesn't exist
			File outputDirectory = new File(o_path);
			if (!outputDirectory.exists()) {
				outputDirectory.mkdirs(); // Ensure directory exists
			}
			// Handle case when no splitting is needed (b_row == 0 && b_col == 0)
			if (b_row == 0 && b_col == 0) {
				// If no split is required, save the entire bottom image directly
				File outputFile = new File(outputDirectory, String.format("%s.png", mName));
				ImageIO.write(bottomImage, "PNG", outputFile);
				images.add(mName); // Add the unsplit image name
			} else {
				// Split the image into rows and columns as needed
				int sectionWidth = b_col > 0 ? bottomImage.getWidth() / b_col : bottomImage.getWidth();
				int sectionHeight = b_row > 0 ? bottomImage.getHeight() / b_row : bottomImage.getHeight();
				// Loop through each row and column to split the image
				for (int col = 0; col < Math.max(b_col, 1); col++) {
					// Calculate the x and y position for the current section
					int x = col * sectionWidth;
					// Get the current section from the bottom image
					BufferedImage bottomSection = bottomImage.getSubimage(x, 0, sectionWidth, sectionHeight);

					// Merge the bottom section with the top image if overlaying
					Graphics2D g = bottomSection.createGraphics();
					g.drawImage(bottomSection, 0, 0, null);
					if (overlay && topImage != null) {
						BufferedImage topSection = topImage.getSubimage(x, 0, sectionWidth, sectionHeight);
						g.drawImage(topSection, 0, 0, null);
					}
					g.dispose();

					// Create the output filename with the format "mainName_row_col.png"
					String outputFileName = String.format("%s_%d.png", mName, col);
					File outputFile = new File(outputDirectory, outputFileName);
					// Add the image name (without extension) to the list
					images.add(String.format("%s_%d", mName, col));
					// Save the split image
					ImageIO.write(bottomSection, "PNG", outputFile);
				}
			}
		} catch (IOException e) {
			logger.print("Error during image processing: " + e.getMessage(), true);
			e.printStackTrace();
		}
		return images;
	}
}
