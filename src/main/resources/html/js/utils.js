// Logging utility
function log(message) {
    console.log(message);
    if (window.JavaBridge) {
        window.JavaBridge.log(message);
    }
}
