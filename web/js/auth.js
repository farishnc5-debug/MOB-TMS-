import { auth } from "./firebase-init.js";
import {
  onAuthStateChanged,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendPasswordResetEmail,
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-auth.js";

const params = new URLSearchParams(window.location.search);
const redirectTo = params.get("redirect") || "index.html";

// Already signed in? Skip straight to the dashboard.
onAuthStateChanged(auth, (user) => {
  if (user) window.location.replace(redirectTo);
});

// ---- DOM ----
const banner = document.getElementById("auth-banner");

const stepEmail = document.getElementById("step-email");
const formEmail = document.getElementById("form-email");
const fieldEmail = document.getElementById("field-email");
const inputEmail = document.getElementById("input-email");
const btnNext = document.getElementById("btn-next");
const toggleMode = document.getElementById("toggle-mode");
const toggleLabel = document.getElementById("toggle-label");
const titleSignin = document.getElementById("title-signin");

const stepPassword = document.getElementById("step-password");
const titlePassword = document.getElementById("title-password");
const ledePassword = document.getElementById("lede-password");
const identityEmail = document.getElementById("identity-email");
const changeEmail = document.getElementById("change-email");
const formPassword = document.getElementById("form-password");
const fieldPassword = document.getElementById("field-password");
const inputPassword = document.getElementById("input-password");
const fieldConfirm = document.getElementById("field-confirm");
const inputConfirm = document.getElementById("input-confirm");
const forgotPassword = document.getElementById("forgot-password");
const btnSubmit = document.getElementById("btn-submit");

let mode = "signin"; // "signin" | "signup"

function setBanner(text, kind) {
  if (!text) {
    banner.className = "auth-banner";
    banner.textContent = "";
    return;
  }
  banner.className = `auth-banner show ${kind}`;
  banner.textContent = text;
}

function setFieldError(fieldEl, show, message) {
  fieldEl.classList.toggle("has-error", !!show);
  if (show && message) fieldEl.querySelector(".field-error").textContent = message;
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function applyMode() {
  const isSignup = mode === "signup";
  titleSignin.textContent = isSignup ? "Create account" : "Sign in";
  titlePassword.textContent = isSignup ? "Create account" : "Sign in";
  ledePassword.textContent = isSignup
    ? "Choose a password to finish creating your account"
    : "Enter your password to continue";
  btnSubmit.textContent = isSignup ? "Create account" : "Sign in";
  fieldConfirm.style.display = isSignup ? "" : "none";
  toggleLabel.textContent = isSignup ? "Already have an account?" : "New here?";
  toggleMode.textContent = isSignup ? "Sign in instead" : "Create an account";
}

toggleMode.addEventListener("click", () => {
  mode = mode === "signin" ? "signup" : "signin";
  applyMode();
});

formEmail.addEventListener("submit", (e) => {
  e.preventDefault();
  const email = inputEmail.value.trim();
  if (!isValidEmail(email)) {
    setFieldError(fieldEmail, true);
    return;
  }
  setFieldError(fieldEmail, false);
  setBanner("", "");
  identityEmail.textContent = email;
  stepEmail.classList.remove("active");
  stepPassword.classList.add("active");
  inputPassword.value = "";
  inputPassword.focus();
});

changeEmail.addEventListener("click", () => {
  stepPassword.classList.remove("active");
  stepEmail.classList.add("active");
  setBanner("", "");
  inputEmail.focus();
});

formPassword.addEventListener("submit", async (e) => {
  e.preventDefault();
  const email = inputEmail.value.trim();
  const password = inputPassword.value;

  setFieldError(fieldPassword, false);
  setFieldError(fieldConfirm, false);

  if (!password) {
    setFieldError(fieldPassword, true, "Enter your password.");
    return;
  }

  if (mode === "signup") {
    if (password.length < 6) {
      setFieldError(fieldPassword, true, "Use at least 6 characters.");
      return;
    }
    if (password !== inputConfirm.value) {
      setFieldError(fieldConfirm, true);
      return;
    }
  }

  btnSubmit.disabled = true;
  btnSubmit.textContent = mode === "signup" ? "Creating account…" : "Signing in…";
  setBanner("", "");

  try {
    if (mode === "signup") {
      await createUserWithEmailAndPassword(auth, email, password);
    } else {
      await signInWithEmailAndPassword(auth, email, password);
    }
    // onAuthStateChanged above handles the redirect.
  } catch (err) {
    setBanner(describeAuthError(err), "error");
    btnSubmit.disabled = false;
    btnSubmit.textContent = mode === "signup" ? "Create account" : "Sign in";
  }
});

forgotPassword.addEventListener("click", async () => {
  const email = inputEmail.value.trim();
  if (!isValidEmail(email)) {
    stepPassword.classList.remove("active");
    stepEmail.classList.add("active");
    setFieldError(fieldEmail, true);
    return;
  }
  try {
    await sendPasswordResetEmail(auth, email);
    setBanner(`Password reset email sent to ${email}.`, "success");
  } catch (err) {
    setBanner(describeAuthError(err), "error");
  }
});

function describeAuthError(err) {
  switch (err.code) {
    case "auth/invalid-email":
      return "That email address doesn't look right.";
    case "auth/user-not-found":
    case "auth/wrong-password":
    case "auth/invalid-credential":
      return "Wrong email or password.";
    case "auth/email-already-in-use":
      return "An account with this email already exists.";
    case "auth/weak-password":
      return "Choose a stronger password (at least 6 characters).";
    case "auth/too-many-requests":
      return "Too many attempts. Please wait a moment and try again.";
    case "auth/network-request-failed":
      return "Network error. Check your connection and try again.";
    default:
      return "Something went wrong. Please try again.";
  }
}

applyMode();
