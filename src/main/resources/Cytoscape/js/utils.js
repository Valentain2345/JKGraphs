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
    
	const validLayouts = ['grid', 'circle', 'concentric', 'breadthfirst', 'cose', 'cose-bilkent'];
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
