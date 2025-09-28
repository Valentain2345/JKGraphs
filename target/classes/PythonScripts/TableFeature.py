import numpy as np
import matplotlib.pyplot as plt
import umap
from sklearn.datasets import make_blobs

# Generate sample data (replace this with your own dataset)
def generate_sample_data():
    # Create a synthetic dataset with 300 samples, 4 clusters, and 10 features
    data, labels = make_blobs(n_samples=300, centers=4, n_features=10, random_state=42)
    return data, labels

# Function to fit UMAP model
def fit_umap(data, n_neighbors=15, min_dist=0.1, n_components=2, random_state=42):
    # Initialize UMAP
    reducer = umap.UMAP(
        n_neighbors=n_neighbors,
        min_dist=min_dist,
        n_components=n_components,
        random_state=random_state
    )
    # Fit the model
    reducer.fit(data)
    return reducer

# Function to fit and transform UMAP
def fit_transform_umap(data, n_neighbors=15, min_dist=0.1, n_components=2, random_state=42):
    # Initialize UMAP
    reducer = umap.UMAP(
        n_neighbors=n_neighbors,
        min_dist=min_dist,
        n_components=n_components,
        random_state=random_state
    )
    # Fit and transform the data
    embedding = reducer.fit_transform(data)
    return embedding, reducer

# Function to transform data using a fitted UMAP model
def transform_umap(data, fitted_reducer):
    # Transform data using the fitted model
    embedding = fitted_reducer.transform(data)
    return embedding

# Function to create and plot UMAP
def plot_umap(embedding, labels=None):
    # Create scatter plot
    plt.figure(figsize=(10, 8))
    if labels is not None:
        # Color points by labels if provided
        scatter = plt.scatter(embedding[:, 0], embedding[:, 1], c=labels, cmap='Spectral', s=50)
        plt.colorbar(scatter, label='Cluster')
    else:
        # Plot without labels
        plt.scatter(embedding[:, 0], embedding[:, 1], s=50)
    
    plt.title('UMAP Projection of Data')
    plt.xlabel('UMAP 1')
    plt.ylabel('UMAP 2')
    plt.tight_layout()
    plt.show()

def main():
    # Generate or load your data
    data, labels = generate_sample_data()
    
    # Option 1: Use fit_transform directly
    embedding, reducer = fit_transform_umap(data, n_neighbors=15, min_dist=0.1)
    plot_umap(embedding, labels=labels)
    
    # Option 2: Use fit and transform separately
    # reducer = fit_umap(data, n_neighbors=15, min_dist=0.1)
    # embedding = transform_umap(data, reducer)
    # plot_umap(embedding, labels=labels)

if __name__ == "__main__":
    main()