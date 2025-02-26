/* Params */
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
let actualName = bossName.value;
let nextStageThreashold = 0;
let animatedMainFrames = '';
let animationHandler = null;
let mathAnimationHandler = null;
let defaultMainTextureAnimation = null;
let defaultOverlayDouble = null;
let defaultOverlayNormal = null;
let hasMadeTable = false;

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
        const duration = parseInt(durationInput.value) || 1;
        const order = parseInt(orderInput.value) || (index + 1);
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
  actualName = bossName.value;
  const threshold = parseFloat(document.getElementById("stageThreshold").value);

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
      const duration = parseInt(durationInput.value) || 1;
      const order = parseInt(orderInput.value) || (i + 1);
      fileReadPromises.push(
        readFileAsDataURL(file).then((dataURL) => {
          // Generate a fileName for packaging.
          const fileName = actualName + "_stage_" + threshold + "_" + i.toString().padStart(5, "0") + ".png";
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
      const duration = parseInt(durationInput.value) || 1;
      const order = parseInt(orderInput.value) || (i + 1);
      fileReadPromises.push(
        readFileAsDataURL(file).then((dataURL) => {
          const fileName = actualName + "_stage_" + threshold + "_" + i.toString().padStart(5, "0") + ".png";
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
  actualName = bossName.value;
  console.log("[Animation] Starting animation for:", actualName);

  const frames = [];
  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    const durationInput = document.getElementById('duration_' + i);
    const orderInput = document.getElementById('order_' + i);
    const duration = parseInt(durationInput.value) || 1;
    const order = parseInt(orderInput.value) || (i + 1);
    console.log(`[Animation] Processing file "${file.name}" with duration=${duration} and order=${order}`);
    try {
      const dataURL = await readFileAsDataURL(file);
      const fileName = actualName + '_animated_main_texture_' + i.toString().padStart(5, '0') + '.png';
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
  actualName = bossName.value;
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
function generateZip() {
  actualName = bossName.value;
  console.log("[ZIP] actualName:", actualName);

  const zip = new JSZip();
  const splitsAmount = healthSplits.value;
  console.log("[ZIP] splitsAmount:", splitsAmount);

  const assetsDir = zip.folder('assets/BetterBossbars/' + actualName);
  const imagesDir = zip.folder('images');
  const hudDir = zip.folder('huds');
  const layoutDir = zip.folder('layouts');
  let FullImageYml = '';

  const bossListener = {
    clazz: 'placeholder',
    value: '(number)bossbar_entity_' + actualName + '_current_health',
    max: '(number)bossbar_entity_' + actualName + '_max_health',
  };

  // Process main texture (static or animated)
  if (!animatedCheckbox.checked) {
    console.log("[ZIP] Processing static main texture...");
    const defaultTextureData = textureImage.dataset.default || textureImage.src;
    console.log("[ZIP] defaultTextureData:", defaultTextureData);
    if (defaultTextureData) {
      const blob = dataURLtoBlob(defaultTextureData);
      if (blob) {
        assetsDir.file(actualName + '_main_texture.png', blob);
        console.log("[ZIP] Added file:", actualName + '_main_texture.png');
      }
      FullImageYml += generateSingleImage(actualName + '_holder', 'assets/BetterBossbars/' + actualName + '/' + actualName + '_main_texture.png') + '\n\n';
      stageMap.set(`${actualName}_default`, {
        images: [{
          name: actualName + '_holder',
          x: 0,
          y: 0
        }],
      });
      console.log("[ZIP] Set default stage in stageMap.");
    }
  }
  else {
    console.log("[ZIP] Processing animated main texture...");
    if (animatedFilesInput && animatedFilesInput.files.length > 0) {
      animatedMainFrames.forEach((frame) => {
        const blob = dataURLtoBlob(frame.src);
        if (blob) {
          assetsDir.file(frame.fileName, blob);
          console.log("[ZIP] Added animated frame file:", frame.fileName);
        }
      });
      FullImageYml += generateSequenceImage(actualName + '_animated_main_texture', animatedMainFrames) + '\n\n';
      stageMap.set(`${actualName}_default`, {
        threshold: 100,
        animated: true,
        images: [{
          name: actualName + '_animated_main_texture',
          x: 0,
          y: 0
        }],
      });
      console.log("[ZIP] Set animated default stage in stageMap.");
    }
  }

  // Process texture stages.
  textureStages.forEach((stage, index) => {
    console.log("[ZIP] Processing texture stage:", stage.threshold);
    if (!stage.animated) {
      const blob = dataURLtoBlob(stage.src);
      if (blob) {
        assetsDir.file(`${actualName}_texture_stage_${stage.threshold}.png`, blob);
        console.log("[ZIP] Added texture stage file:", `${actualName}_texture_stage_${stage.threshold}.png`);
        FullImageYml += generateSingleImage(
          actualName + `${actualName}_texture_stage__${stage.threshold}`,
          "assets/BetterBossars/" + actualName + "/" + `${actualName}_texture_stage_${stage.threshold}.png`
        ) + "\n\n";
        stageMap.set(`${actualName}_stage_${stage.threshold}`, {
          threshold: stage.threshold,
          animated: false,
          images: [{
            name: `${actualName}_texture_stage_${stage.threshold}`,
            x: 0,
            y: 0
          }]
        });
        console.log("[ZIP] Set non-animated stage in stageMap for threshold", stage.threshold);
      }
    } else {
      console.log("[ZIP] Processing animated texture stage:", stage.threshold);
      // For animated stages, iterate through animatedFrames.
      stage.animatedFrames.forEach((frame) => {
        const blob = dataURLtoBlob(frame.src);
        if (blob) {
          assetsDir.file(`${frame.fileName}`, blob);
          console.log("[ZIP] Added animated stage frame file:", frame.fileName);
        }
      });
      FullImageYml += generateSequenceImage(
        `${actualName}_stage_${stage.threshold}`,
        stage.animatedFrames
      ) + "\n\n";
      stageMap.set(`${actualName}_stage_${stage.threshold}`, {
        threshold: stage.threshold,
        animated: true,
        animConfig: stage.animConfig,
        animatedFrames: stage.animatedFrames,
        images: [{
          name: `${actualName}_texture_stage_${stage.threshold}`,
          x: 0,
          y: 0
        }]
      });
      console.log("[ZIP] Set animated stage in stageMap for threshold", stage.threshold);
    }
  });

  // Process overlay images based on bossbarType.
  if (bossbarTypeSelect.value === 'normal') {
    console.log("[ZIP] Processing overlay for normal mode...");
    const defaultOverlayData = overlayImage.dataset.default || overlayImage.src;
    if (defaultOverlayData) {
      const blob = dataURLtoBlob(defaultOverlayData);
      if (blob) {
        assetsDir.file(actualName + '_main_overlay.png', blob);
        console.log("[ZIP] Added main overlay file:", actualName + '_main_overlay.png');
      }
      FullImageYml += generateListenerImage(
        actualName + '_main_overlay',
        'assets/BetterBossars/' + actualName + '/' + actualName + '_main_overlay.png',
        splitsAmount,
        reversedCheckbox.checked ? 'right' : 'left',
        bossListener
      ) + '\n\n';
      const defaultStage = stageMap.get(`${actualName}_default`);
      if (defaultStage) {
        defaultStage.images.push({
          name: actualName + '_main_overlay',
          x: offsetXInput.value,
          y: offsetYInput.value
        });
        console.log("[ZIP] Updated default stage images with overlay.");
      }
    }
    overlayStagesNormal.forEach((stage, index) => {
      if (nextStageThreashold <= stage.threshold) nextStageThreashold = stage.threshold;
      const blob = dataURLtoBlob(stage.src);
      if (blob) {
        assetsDir.file(`${actualName}_overlay_stage_${index}_${stage.threshold}.png`, blob);
        console.log("[ZIP] Added overlay stage file (normal):", `${actualName}_overlay_stage_${index}_${stage.threshold}.png`);
        FullImageYml += generateListenerImage(
          `${actualName}_overlay_stage_${index}_${stage.threshold}`,
          'assets/BetterBossars/' + actualName + '/' + `${actualName}_overlay_stage_${index}_${stage.threshold}.png`,
          splitsAmount,
          reversedCheckbox.checked ? 'right' : 'left',
          bossListener
        ) + '\n\n';
        const stageEntry = stageMap.get(`${actualName}_stage_${stage.threshold}`);
        if (stageEntry) {
          // If stageEntry.threshold is a number, do not call push.
          // Instead, update it if needed.
          // Here we assume threshold is stored as a number.
          stageEntry.threshold = stage.threshold;
          stageEntry.images.push({
            name: `${actualName}_overlay_stage_${index}_${stage.threshold}`,
            x: offsetXInput.value,
            y: offsetYInput.value
          });
          console.log("[ZIP] Updated stage entry images for overlay (normal) at threshold", stage.threshold);
        }
      }
    });
  }
  else {
    console.log("[ZIP] Processing overlay for double mode...");
    const defaultOverlayLeft = overlayImageLeft.dataset.default || overlayImageLeft.src;
    const defaultOverlayRight = overlayImageRight.dataset.default || overlayImageRight.src;
    if (defaultOverlayLeft) {
      const blob = dataURLtoBlob(defaultOverlayLeft);
      if (blob) {
        assetsDir.file(actualName + '_main_overlay_left.png', blob);
        console.log("[ZIP] Added main overlay left file:", actualName + '_main_overlay_left.png');
      }
      FullImageYml += generateListenerImage(
        actualName + '_main_overlay_left',
        'assets/BetterBossars/' + actualName + '/' + actualName + '_main_overlay_left.png',
        splitsAmount,
        reversedCheckbox.checked ? 'right' : 'left',
        bossListener
      ) + '\n\n';
      let defaultStage = stageMap.get(`${actualName}_default`);
      if (defaultStage) {
        defaultStage.images.push({
          name: actualName + '_main_overlay_left',
          x: offsetLeftXInput.value,
          y: offsetLeftYInput.value
        });
        console.log("[ZIP] Updated default stage with main overlay left.");
      }
    }
    if (defaultOverlayRight) {
      const blob = dataURLtoBlob(defaultOverlayRight);
      if (blob) {
        assetsDir.file(actualName + '_main_overlay_right.png', blob);
        console.log("[ZIP] Added main overlay right file:", actualName + '_main_overlay_right.png');
      }
      FullImageYml += generateListenerImage(
        actualName + '_main_overlay_right',
        'assets/BetterBossars/' + actualName + '/' + actualName + '_main_overlay_right.png',
        splitsAmount,
        reversedCheckbox.checked ? 'left' : 'right',
        bossListener
      ) + '\n\n';
      let defaultStage = stageMap.get(`${actualName}_default`);
      if (defaultStage) {
        defaultStage.images.push({
          name: actualName + '_main_overlay_right',
          x: offsetRightXInput.value,
          y: offsetRightYInput.value
        });
        console.log("[ZIP] Updated default stage with main overlay right.");
      }
    }
    overlayStagesDouble.forEach((stage, index) => {
      // For double mode, index is not used for filename generation; we use stage.threshold.
      const blobLeft = dataURLtoBlob(stage.left);
      const blobRight = dataURLtoBlob(stage.right);
      if (nextStageThreashold < stage.threshold) nextStageThreashold = stage.threshold;
      if (blobLeft) {
        assetsDir.file(`${actualName}_overlay_stage_${stage.threshold}_left.png`, blobLeft);
        console.log("[ZIP] Added overlay stage file (double left):", `${actualName}_overlay_stage_${stage.threshold}_left.png`);
      }
      if (blobRight) {
        assetsDir.file(`overlay_stage_${stage.threshold}_right.png`, blobRight);
        console.log("[ZIP] Added overlay stage file (double right):", `overlay_stage_${stage.threshold}_right.png`);
      }
      FullImageYml += generateListenerImage(
        `${actualName}_overlay_stage_${stage.threshold}_left`,
        'assets/BetterBossars/' + actualName + '/' + `${actualName}_overlay_stage_${stage.threshold}_left.png`,
        splitsAmount,
        reversedCheckbox.checked ? 'reversed' : 'normal',
        bossListener
      ) + '\n\n';
      FullImageYml += generateListenerImage(
        `${actualName}_overlay_stage_${stage.threshold}_right`,
        'assets/BetterBossars/' + actualName + '/' + `${actualName}_overlay_stage_${stage.threshold}_right.png`,
        splitsAmount,
        reversedCheckbox.checked ? 'left' : 'right',
        bossListener
      ) + '\n\n';
      let contextStage = stageMap.get(`${actualName}_stage_${stage.threshold}`);
      if (contextStage) {
        // Update contextStage images.
        contextStage.threshold = stage.threshold; // update threshold
        contextStage.images.push({
          name: `${actualName}_overlay_stage_${stage.threshold}_left`,
          x: offsetLeftXInput.value,
          y: offsetLeftYInput.value
        });
        contextStage.images.push({
          name: `${actualName}_overlay_stage_${stage.threshold}_right`,
          x: offsetRightXInput.value,
          y: offsetRightYInput.value
        });
        console.log("[ZIP] Updated stage entry for double overlay at threshold", stage.threshold);
      }
    });
  }
  let ymlNamePreix = "";
  if (document.getElementById("useNameForYmlCheckbox").checked) ymlNamePreix = `${actualName.toLowerCase()}`;
  else ymlNamePreix = "better";

  imagesDir.file(ymlNamePrefix + "-bossbar-images.yml", FullImageYml);

  const FullLayoutYml = generateLayoutYamlFromMap(stageMap, actualName);
  layoutDir.file(ymlNamePrefix + "-bossbar-layouts.yml", FullLayoutYml);

  const FullHUD = generateHudYaml(layoutMap);
  hudDir.file(ymlNamePrefix + "-bossbar-hud.yml", FullHUD);

  zip.generateAsync({ type: "blob" }).then(function(content) {
    const a = document.createElement("a");
    a.href = URL.createObjectURL(content);
    a.download = actualName + "-bossbar.zip";
    a.click();
  });
}
function generateSingleImage(imageID, imageName) {
  return `${imageID}:
  type: single
  file: "${imageName}"`;
}
function generateListenerImage(imageID, imageName, splits, splitType, listener) {
  let yaml = `${imageID}:
  type: listener
  file: "${imageName}"
  split: ${splits}
  split-type: ${splitType}
  setting:
    listener:
      class: ${listener.clazz}\n`;
  if (listener.value != null) {
    yaml += `      value: "${listener.value}"\n`;
  }
  if (listener.max != null) {
    yaml += `      max: "${listener.max}"\n`;
  }
  return yaml;
}
function generateLayoutYaml(stageName, images, actualName, threshold, isAnimated, animConfig) {
  let yaml = `${stageName}:\n  images:\n`;
  images.forEach((img, index) => {
    yaml += `    ${index + 1}:\n`;
    yaml += `      name: ${img.name}\n`;
    yaml += `      x: ${img.x}\n`;
    yaml += `      y: ${img.y}\n`;
  });
  if (isAnimated && animConfig) {
    yaml += `  animations:\n`;
    yaml += `    type: ${animConfig.type}\n`;
    yaml += `    duration: ${animConfig.duration}\n`;
    yaml += `    x-equation: ${animConfig.xEquation}\n`;
    yaml += `    y-equation: ${animConfig.yEquation}\n`;
  }

  if (threshold !== undefined && threshold < 100) {
    yaml += `  condition:\n`;
    yaml += `    1:\n`;
    yaml += `      first: (number)papi:bossbar_entity_${actualName}_max_health * ${threshold / 100}\n`;
    yaml += `      second: (number)papi:bossbar_entity_${actualName}_current_health\n`;
    yaml += `      operation: "<="\n`;
  }
  else {
    yaml += `  condition:\n`;
    yaml += `    1:\n`;
    yaml += `      first: (number)papi:bossbar_entity_${actualName}_max_health * ${nextStageThreashold / 100}\n`;
    yaml += `      second: (number)papi:bossbar_entity_${actualName}_current_health\n`;
    yaml += `      operation: ">="\n`;
  }
  yaml += `    2:\n`;
  yaml += `      first: "papi:bossbar_entity_${actualName}_exists"\n`;
  yaml += `      second: "true"\n`;
  yaml += `      operation: "=="\n`;
  yaml += `    3:\n`;
  yaml += `      first: "papi:bossbar_entity_${actualName}_isWithinThresholdDistance"\n`;
  yaml += `      second: "true"\n`;
  yaml += `      operation: "=="\n`;
  return yaml;
}
function generateLayoutYamlFromMap(stageMapB, actualName, animConfig) {
  let yaml = '';
  let index = 0;
  for (const [stageName, stageData] of stageMapB.entries()) {
    yaml += generateLayoutYaml(stageName, stageData.images, actualName, stageData.threshold, stageData.animated, animConfig);
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
      yaml += `    - "assets/BetterBossbars/${actualName}/${frame.fileName}":${frame.duration}\n`;
    } else {
      yaml += `    - "assets/BetterBossbars/${actualName}/${frame.fileName}"\n`;
    }
  });
  return yaml;
}
function generateHudYaml(layouts) {
  let yaml = 'better_bossbars:\n  layouts:\n';
  layouts.forEach((index, layout) => {
    yaml += `    ${index + 1}:\n`;
    yaml += `      name: ${layout}\n`;
    yaml += `      x: 50\n`;
    yaml += `      y: 0\n`;
  });
  return yaml;
}
function updateOverlayCrop() {
  const healthPercent = healthSlider.value / 100;
  const containerWidth = bossbarDisplay.clientWidth;
  const scaleValue = parseFloat(getComputedStyle(bossbarDisplay).getPropertyValue('--scale')) || 1;
  if (bossbarTypeSelect.value === 'normal') {
    const posX = parseInt(offsetXInput.value) || 0;
    const posY = parseInt(offsetYInput.value) || 0;

    overlayImage.style.transform = `translate(${posX * scaleValue}px, ${posY * scaleValue}px) scale(${scaleValue})`;
    const visibleWidth = containerWidth * healthPercent;
    const cropPx = containerWidth - visibleWidth;
    if (!reversedCheckbox.checked) {
      overlayImage.style.clipPath = `inset(0 ${cropPx}px 0 0)`;
    } else {
      overlayImage.style.clipPath = `inset(0 0 0 ${cropPx}px)`;
    }
    overlayImage.style.display = 'block';
    overlayImageLeft.style.display = 'none';
    overlayImageRight.style.display = 'none';
  }
  else if (bossbarTypeSelect.value === 'double') {
    const halfWidth = containerWidth / 2;
    const leftPosX = parseInt(offsetLeftXInput.value) || 0;
    const leftPosY = parseInt(offsetLeftYInput.value) || 0;
    overlayImageLeft.style.transform = `translate(${leftPosX * scaleValue}px, ${leftPosY * scaleValue}px) scale(${scaleValue})`;
    const rightPosX = parseInt(offsetRightXInput.value) || 0;
    const rightPosY = parseInt(offsetRightYInput.value) || 0;
    overlayImageRight.style.transform = `translate(${rightPosX * scaleValue}px, ${rightPosY * scaleValue}px) scale(${scaleValue})`;
    const visibleHalf = halfWidth * healthPercent;
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
    stop: function() {
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
    stop: function() {
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
      return {src: document.getElementById('textureStageFile').files[0]};
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