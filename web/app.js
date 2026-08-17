const state = {
  power: false,
  temperature: 24,
  mode: "COOL",
  fan: "AUTO",
  verticalSwing: false,
  lamp: true,
  turbo: false,
  timerOnHours: null,
  timerOffHours: null
};

const modes = ["COOL", "DRY", "FAN"];
const fans = ["AUTO", "LOW", "MEDIUM", "HIGH"];
const $ = (id) => document.getElementById(id);

function vibrate() {
  if (navigator.vibrate) navigator.vibrate(25);
}

function bridgeAvailable() {
  return Boolean(window.AndroidIR?.sendState);
}

function sendToIrBridge(action, payload = {}) {
  const message = { action, state: { ...state }, ...payload };
  console.log("IR command:", message);

  if (bridgeAvailable()) {
    window.AndroidIR.sendState(JSON.stringify(message));
  } else {
    updateBridgeStatus("Browser mode: UI works, but IR requires the Android app.");
  }
}

window.updateBridgeStatus = function (message) {
  $("bridgeStatus").textContent = message;
};

function render() {
  $("temperature").textContent = state.temperature;
  $("powerStatus").textContent = state.power ? "ON" : "OFF";
  $("powerStatus").classList.toggle("off", !state.power);
  $("modeStatus").textContent = state.mode;
  $("fanStatus").textContent = state.fan;
  $("vSwingStatus").textContent = state.verticalSwing ? "ON" : "OFF";
  $("lampStatus").textContent = state.lamp ? "ON" : "OFF";
  $("turboStatus").textContent = state.turbo ? "ON" : "OFF";
  $("timerOnStatus").textContent = state.timerOnHours ? `${state.timerOnHours}H` : "OFF";
  $("timerOffStatus").textContent = state.timerOffHours ? `${state.timerOffHours}H` : "OFF";
}

function commit(action, payload = {}) {
  render();
  vibrate();
  sendToIrBridge(action, payload);
}

$("powerBtn").addEventListener("click", () => {
  state.power = !state.power;
  commit("power");
});

document.querySelectorAll("[data-action]").forEach((btn) => {
  btn.addEventListener("click", () => {
    const action = btn.dataset.action;

    if (action === "temp-up") state.temperature = Math.min(30, state.temperature + 1);
    if (action === "temp-down") state.temperature = Math.max(16, state.temperature - 1);

    if (action === "mode") {
      state.mode = modes[(modes.indexOf(state.mode) + 1) % modes.length];
      if (state.mode === "DRY") {
        state.temperature = 24;
        state.fan = "LOW";
      }
      if (state.mode === "FAN") state.fan = "HIGH";
    }

    if (action === "fan") {
      state.fan = fans[(fans.indexOf(state.fan) + 1) % fans.length];
    }

    if (action === "v-swing") state.verticalSwing = !state.verticalSwing;
    if (action === "lamp") state.lamp = !state.lamp;

    if (action === "turbo") {
      state.turbo = !state.turbo;
      if (state.turbo) state.mode = "COOL";
    }

    commit(action);
  });
});

function populateHours(id) {
  const select = $(id);
  for (let hour = 1; hour <= 15; hour++) {
    const option = document.createElement("option");
    option.value = hour;
    option.textContent = `${hour} ${hour === 1 ? "hour" : "hours"}`;
    select.appendChild(option);
  }
}

populateHours("timerOnHours");
populateHours("timerOffHours");

$("setTimerOnBtn").addEventListener("click", () => {
  const hours = Number($("timerOnHours").value);
  state.timerOnHours = hours;
  commit("timer-set", { timerType: "on", hours });
});

$("cancelTimerOnBtn").addEventListener("click", () => {
  state.timerOnHours = null;
  commit("timer-cancel", { timerType: "on" });
});

$("setTimerOffBtn").addEventListener("click", () => {
  const hours = Number($("timerOffHours").value);
  state.timerOffHours = hours;
  commit("timer-set", { timerType: "off", hours });
});

$("cancelTimerOffBtn").addEventListener("click", () => {
  state.timerOffHours = null;
  commit("timer-cancel", { timerType: "off" });
});

$("themeBtn").addEventListener("click", () => {
  document.body.classList.toggle("dark");
  $("themeBtn").textContent = document.body.classList.contains("dark") ? "☀" : "☾";
});

render();
updateBridgeStatus(
  bridgeAvailable()
    ? "Android IR bridge connected ✓"
    : "Browser mode: UI works, but IR requires the Android app."
);
