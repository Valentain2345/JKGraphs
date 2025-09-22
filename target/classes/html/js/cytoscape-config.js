// Cytoscape style and layout config using palette
function getCytoscapeConfig(palette) {
    // Randomly choose black or white for background
    const backgrounds = ['#000000', '#ffffff'];
    const backgroundColor = backgrounds[Math.floor(Math.random() * backgrounds.length)];
    // Compute edge color as contrast with background
    const edgeColor = getContrastingColor([backgroundColor]);
    return {
        style: [
            {
                selector: 'node',
                style: {
                    'shape': 'ellipse',
                    'width': 90,
                    'height': 90,
                    'background-color': 'data(color)',
                    'background-image': 'data(backgroundImage)',
                    'background-fit': 'cover',
                    'background-clip': 'node',
                    'label': 'data(id)',
                    'text-valign': 'center',
                    'text-halign': 'center',
                    'color': '#fff',
                    'font-size': '20px',
                    'font-weight': 'bold',
                    'text-outline-width': 3,
                    'text-outline-color': 'data(borderColor)',
                    'border-width': 6,
                    'border-color': 'data(borderColor)',
                    'shadow-color': 'data(borderColor)',
                    'shadow-blur': 16,
                    'shadow-opacity': 0.7,
                    'shadow-offset-x': 0,
                    'shadow-offset-y': 0,
                    'transition-property': 'border-color, width, height, background-color',
                    'transition-duration': '0.3s'
                }
            },
            {
                selector: 'node:hover',
                style: {
                    'border-color': '#1A4A7A',
                    'shadow-opacity': 0.5
                }
            },
            {
                selector: 'node:selected',
                style: {
                    'width': 100,
                    'height': 100,
                    'border-width': 8,
                    'border-color': '#FFD700', // gold
                    'color': '#1A4A7A', // highlight text color
                    'text-outline-width': 3,
                    'text-outline-color': '#FFD700', // highlight text outline
                    'z-compound-depth': 'top'
                }
            },
            {
                selector: 'edge',
                style: {
                    'width': 4,
                    'line-color': '#FF9800', // vivid orange for contrast
                    'target-arrow-color': '#FF9800',
                    'target-arrow-shape': 'triangle',
                    'curve-style': 'bezier',
                    'label': 'data(label)',
                    'font-size': '12px',
                    'color': '#222',
                    'text-outline-width': 2,
                    'text-outline-color': '#fff',
                    'text-rotation': 'autorotate',
                    'text-margin-y': -10,
                    'transition-property': 'line-color',
                    'transition-duration': '0.3s'
                }
            },
            {
                selector: 'edge:hover',
                style: {
                    'line-color': '#7A8B9A',
                    'target-arrow-color': '#7A8B9A'
                }
            },
            {
                selector: 'edge:selected',
                style: {
                    'line-color': '#FFD700', // gold highlight
                    'target-arrow-color': '#FFD700',
                    'width': 5
                }
            }
        ],
        layout: {
            name: 'circle',
            idealEdgeLength: 100,
            nodeOverlap: 20,
            refresh: 20,
            fit: true,
            padding: 30,
            randomize: false,
            componentSpacing: 100,
            nodeRepulsion: 400000,
            edgeElasticity: 100,
            nestingFactor: 5,
            gravity: 80,
            numIter: 1000,
            initialTemp: 200,
            coolingFactor: 0.95,
            minTemp: 1.0
        },
        background: backgroundColor
    };
}