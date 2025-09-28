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
        // Inject dynamic node size function into style
        cyConfig.style.forEach(function(styleObj) {
            if (styleObj.selector === 'node') {
                styleObj.style.width = getNodeSize;
                styleObj.style.height = getNodeSize;
            }
        });
        window.cy = cytoscape({
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

// Keep only window.setLayout to prevent recursion
window.setLayout = function(layoutName) {
    if (window.cy) {
        changeLayout(window.cy, layoutName);
    } else {
        log('Cytoscape instance not initialized');
    }
};

window.saveAsPNG = function() {
    if (window.cy) {
        saveAsPNG(window.cy);
    } else {
        log('Cytoscape instance not initialized');
    }
};

window.addEventListener('resize', function() {
    if (window.cy) {
        window.cy.resize();
    }
});

window.reloadCytoscape = function() {
    log('Reload requested');
	
};

window.getPNGBase64 = function() {
    if (window.cy) {
        try {
            var pngDataUrl = window.cy.png({ full: true, scale: 2, output: 'base64' });
            var base64 = pngDataUrl.replace(/^data:image\/png;base64,/, '');
            return base64;
        } catch (e) {
            log('Error getting PNG base64: ' + e);
            return null;
        }
    } else {
        log('Cytoscape instance not initialized');
        return null;
    }
};

window.nodeSizeMetric = 'out'; // Default metric

window.setNodeSizeMetric = function(metric) {
    window.nodeSizeMetric = metric === 'in' ? 'in' : 'out';
    if (window.cy) {
        window.cy.style().update(); // Refresh style to apply new sizing
    }
};

// Helper to compute node size based on selected metric
function getNodeSize(node) {
    if (!window.nodeSizeMetric || window.nodeSizeMetric === 'out') {
        return 60 + 10 * node.outgoers('edge').length;
    } else {
        return 60 + 10 * node.incomers('edge').length;
    }
}