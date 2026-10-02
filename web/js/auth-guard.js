// Gates the dashboard behind Firebase Auth sign-in. The recipient-facing track.html
// stays public by design (see README) — only the operator dashboard is gated.
import { auth } from "./firebase-init.js";
import {
  onAuthStateChanged,
  signOut,
} from "https://www.gstatic.com/firebasejs/10.13.0/firebase-auth.js";

const gate = document.getElementById("auth-gate");
const topbarUser = document.getElementById("topbar-user");
const userEmailEl = document.getElementById("user-email");
const signoutBtn = document.getElementById("signout-btn");

function goToLogin() {
  const here = window.location.pathname.split("/").pop() || "index.html";
  window.location.replace(`login.html?redirect=${encodeURIComponent(here)}`);
}

onAuthStateChanged(auth, (user) => {
  if (!user) {
    goToLogin();
    return;
  }
  gate.style.display = "none";
  topbarUser.hidden = false;
  userEmailEl.textContent = user.email;
});

signoutBtn.addEventListener("click", async () => {
  await signOut(auth);
  goToLogin();
});
