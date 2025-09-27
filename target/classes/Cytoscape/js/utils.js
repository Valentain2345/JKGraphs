// Logging utility
function log(message) {
    console.log(message);
    if (window.JavaBridge) {
        window.JavaBridge.log(message);
    }
}
//Change layout function
function changeLayout(cy, layoutName) {
    
	if (!cy) {
        log('Cytoscape instance not found');
        return;
    }
    
	const validLayouts = ['grid', 'circle', 'concentric', 'breadthfirst', 'cose','random'];
    if (!validLayouts.includes(layoutName)) {
        log('Invalid layout name: ' + layoutName);
        return;
    }
    
	try {
        const layout = cy.layout({ name: layoutName, animate: true, animationDuration: 500 });
        layout.run();
        log('Layout changed to ' + layoutName);
    } catch (e) {
        log('Error changing layout: ' + e);
    }
	
}

function saveAsPNG(cy) {
	    if (!cy) {
			 log('Cytoscape instance not found');
			return;
			}
			try {
			const pngData = cy.png({ full: true, scale: 2 });
			const link = document.createElement('a');
			link.href = pngData;
			link.download = 'graph.png';
			document.body.appendChild(link);
			link.click();
			document.body.removeChild(link);
			log('Graph saved as PNG');
			} catch (e) {
				log('Error saving PNG: ' + e);
				}
}

