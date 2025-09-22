// Vivid color palette generation and contrast calculation
const VIVID_COLORS = [
    "#e53935", // Red
    "#d81b60", // Pink
    "#8e24aa", // Purple
    "#5e35b1", // Deep Purple
    "#3949ab", // Indigo
    "#1e88e5", // Blue
    "#039be5", // Light Blue
    "#00acc1", // Cyan
    "#00897b", // Teal
    "#43a047", // Green
    "#7cb342", // Light Green
    "#c0ca33", // Lime
    "#fdd835", // Yellow
    "#ffb300", // Amber
    "#fb8c00", // Orange
    "#f4511e", // Deep Orange
    "#6d4c41", // Brown
    "#757575", // Grey
    "#546e7a"  // Blue Grey
];

function getRandomVividColor() {
    const idx = Math.floor(Math.random() * VIVID_COLORS.length);
    return VIVID_COLORS[idx];
}

function generateColorPalette(count) {
    const palette = [];
    for (let i = 0; i < count; i++) {
        palette.push(getRandomVividColor());
    }
    return palette;
}

function getContrastingColor(colors) {
    const avg = colors.reduce((acc, color) => {
        const r = parseInt(color.slice(1, 3), 16);
        const g = parseInt(color.slice(3, 5), 16);
        const b = parseInt(color.slice(5, 7), 16);
        return [acc[0] + r, acc[1] + g, acc[2] + b];
    }, [0, 0, 0]).map(c => Math.round(c / colors.length));
    const contrast = avg.map(c => 255 - c);
    return `#${contrast.map(c => c.toString(16).padStart(2, '0')).join('')}`;
}