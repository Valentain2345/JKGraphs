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
    
	const validLayouts = [
  'grid', 'circle', 'concentric', 'breadthfirst', 'cose', 'random', 'avsdf',
  'cola', 'dagre', 'elk', 'fcose', 'cose-bilkent', 'cise'
];

const layoutConfigs = {
       grid: {
         name: "grid",
         rows: undefined,
         animate: true,
         animationDuration: 500,
         padding: 30,
       },
       avsdf: {
       name: 'avsdf',
       animate: "end",
       animationDuration: 1000,
       animationEasing: 'ease-in-out',
       nodeSeparation: 500
       },
       circle: {
         name: "circle",
         animate: true,
         animationDuration: 500,
         padding: 30,
         radius: undefined,
       },
       concentric: {
         name: "concentric",
         animate: true,
         animationDuration: 500,
         padding: 30,
         minNodeSpacing: 200,
         concentric: (node) => node.degree(),
         levelWidth: () => 1,
       },
       breadthfirst: {
         name: "breadthfirst",
         animate: true,
         animationDuration: 500,
         padding: 30,
         directed: true,
         spacingFactor: 1.5,
       },
       cose: {
         name: "cose",
         animate: "end",
         animationDuration: 500,
         padding: 30,
         nodeOverlap: 20,
         componentSpacing: 40,
         nodeRepulsion: 400000,
         idealEdgeLength: 100,
         edgeElasticity: 100,
         nestingFactor: 5,
         gravity: 80,
         numIter: 1000,
         initialTemp: 200,
         coolingFactor: 0.95,
         minTemp: 1.0,
       },
       cola: {
         name: "cola",
         animate: true,
         animationDuration: 500,
         padding: 30,
         maxSimulationTime: 3000,
         nodeSpacing: 500,
         edgeLength: 1000,
         infinite: false,
       },
       klay: {
         name: "klay",
         animate: true,
         animationDuration: 500,
         padding: 30,
         nodePlacement: "INTERACTIVE",
         thoroughness: 7,
         spacing: 500,
       },
       fcose: {
         name: "fcose",
         quality: "proof",
         animate: true,
         animationDuration: 500,
         randomize: true,
         padding: 30,
         nodeSeparation: 500,
         idealEdgeLength: (edge) => 300,
         nodeRepulsion: (node) => 4500,
       },
       "cose-bilkent": {
         name: "cose-bilkent",
         animate: true,
         animationDuration: 500,
         padding: 30,
         nodeDimensionsIncludeLabels: true,
         idealEdgeLength: 50,
         nodeRepulsion: 4500,
         gravity: 0.25,
         gravityRange: 3.8,
       },
       cise: {
         name: "cise",
         animate: true,
         animationDuration: 500,
         padding: 30,
         clusters: [], // Auto-detect clusters
         allowNodesInsideCircle: true,
         maxRatioOfNodesInsideCircle: 0.1,
         springCoeff: 0.45,
         nodeRepulsion: 4500,
         gravity: 0.25,
         gravityRange: 3.8,
       },
       elk: {
         name: "elk",
         animate: true,
         animationDuration: 500,
         padding: 30,
         algorithm: "layered", // 'layered', 'force', 'mrtree', 'radial', 'stress'
         nodeDimensionsIncludeLabels: true,
         randomize: true,
         "elk.spacing.nodeNode": 120,
         "elk.layered.spacing.nodeNodeBetweenLayers": 100,
       },
       dagre: {
         name: "dagre",
         animate: true,
         animationDuration: 500,
         padding: 30,
         rankDir: "TB",
         rankSep: 200,
         edgeSep: 50,
       },
     };


    if (!validLayouts.includes(layoutName)) {
        log('Invalid layout name: ' + layoutName);
        return;
    }
    
	try {
        const config = layoutConfigs[layoutName] || { name: layoutName, animate: true, animationDuration: 500 };
        const layout = cy.layout(config);
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
