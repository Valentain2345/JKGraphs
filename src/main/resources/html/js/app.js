// Main entry point for loading Cytoscape data and palette from Java
window.loadCytoscapeData = function(data, palette) {
    try {
        if (!data || !data.elements) {
            log('No elements found in data');
            return;
        }
        // Always assign a new random color to each node on reload
        data.elements.forEach(function(el) {
            if (el.data && el.data.id && !el.data.source && !el.data.target) { // node
                el.data.color = getRandomVividColor();
                el.data.borderColor = getContrastingColor([el.data.color]);
                // Node SVG with vivid color
                const svg = `<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 640'><path fill='${el.data.color}' d='M64 320C64 178.6 178.6 64 320 64C461.4 64 576 178.6 576 320C576 461.4 461.4 576 320 576C178.6 576 64 461.4 64 320z'/></svg>`;
                el.data.backgroundImage = 'data:image/svg+xml;utf8,' + encodeURIComponent(svg);
            }
        });
        var cyConfig = getCytoscapeConfig(palette);
        var cy = cytoscape({
            container: document.getElementById('cy'),
            elements: data.elements,
            style: cyConfig.style,
            layout: cyConfig.layout
        });
        // Set background color
        document.getElementById('cy').style.backgroundColor = cyConfig.background || '#f5f6fa';
        log('Cytoscape graph loaded with ' + data.elements.length + ' elements');
    } catch (e) {
        log('Error loading Cytoscape data: ' + e);
    }
};

// Optionally, reload graph if needed
window.reloadCytoscape = function() {
    log('Reload requested');
    // This can be extended to reload data if needed
};