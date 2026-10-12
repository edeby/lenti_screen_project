# Organoid Phenotype Analysis

A collection of scripts for detecting and quantifying phenotypes in **5x brightfield images** of organoid cultures, developed for a phenotypic CRISPR-Cas9 organoid screen.

# Overview

The pipeline is divided into multiple steps, and works with brightfield images

> Note: The current pipeline was designed to work with images taken with a 5x objective. 
If you are using an objective with different magnification, you might need to adjust parameters and to retrain the cellpose model.

1. Image preparation & preprocessing (background correction, channel & image renaming, QuPath metadata)
2. Individual organoids detection with a custom-trained **Cellpose** model
3. **Ridge thickness** measurement of cystic organoids
4. Manual thickness corrections
5. Per-image results export

# Workflow

The scripts are organized and numbered in a certain order and **have to be run in that order**

| Step | Folder | Purpose |
|------|--------|---------|
| 0 | `0 - Image preprocessing` | Preprocess images with a background correction with Fiji|
| 1 | `1 - Organoid analysis` | Segment, classify & measure organoids with QuPath |
| 2 | `2 - Data analysis` | Analyze organoid metrics with Python |


## 0 - Image preprocessing

#### Requirements
This first part requires Fiji software, which can be downloaded [here](https://imagej.net/software/fiji/downloads). 
No extra update-site is required.

#### How to use
- Download the `.ijm` script
- Drag-drop it into Fiji main window to open it.
- ????

#### Scripts description
| Step | Script | Purpose |
|------|--------|---------|
| 0 | `0-Background correct and flatten` | Applies a background correction to images  |


## 1 - Organoid analysis

#### Requirements
This second part makes the use of [QuPath](https://qupath.github.io/) software.

#### How to use
- Download the `.groovy` scripts
- Drag-drop them into QuPath main window to open them.
- Read carefully the instructions written in the script header, if applicable.
- Click on Run.

#### Scripts description
| Step | Script | Purpose |
|------|--------|---------|
| 0 | `0 - Rename images and add metadata` | Renames images based on the input folder structure, adds metadata |
| 1 | `1 - Set Channel Names and Colors` | Sets the brightfield channel information. |
| 2 | `2 - Run Cellpose prediction` | Detects organoids in the brightfield images using the custom Cellpose model. |
| 3 | `3 - Convert detections to annotations` | Converts the organoid detections into annotations, so they can be manually reviewed and edited. |
| 4 | `4 - Check Thickness` | Measures the ridge thickness of organoids classified as cystic. |
| 5 | `5 - Add manual thickness measurement and objects` | Adds manual corrections and computes the final statistics. Only needed if manual annotations were made **after** running step 4. |
| 6 | `6 - Create Empty objects in images with nothing annotated` | Ensures that images with no annotations or detections still get a row in the exported results, so no image is silently dropped from the dataset. |

#### Cellpose model

Organoid detection relies on a custom Cellpose model trained on brightfield organoid images acquired on 5x objective.
The reference of the model is [DOI:10.5281/zenodo.23230322](https://zenodo.org/doi/10.5281/zenodo.23230322)


## 2 - Data analysis

#### Requirements
This last part relies on Python. *Here, you should explain how to install the necessary packages*

#### How to use

#### Scripts description
| Step | Script | Purpose |
|------|--------|---------|
| 7 | `****` | *add your description*  |



# Citing

Please cite those scripts by linking them to this GitHub or to the release you used, 
and by linking them to corresponding Zenodo entry


## Code authorship
**Author**: Olivier Burri @lacan (1)

**Contributors**: EPFL BioImaging and Optics Platform (BIOP) members


**Affiliations**

(1) EPFL BioImaging and Optics Platform (BIOP)

### If you use thoses scripts, you should cite the following publications

- *insert your publication here*
