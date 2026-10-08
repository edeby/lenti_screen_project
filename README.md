# Organoid Phenotype Analysis

A collection of scripts for detecting and quantifying phenotypes in **5x brightfield images** of organoid cultures, developed for a phenotypic CRISPR-Cas9 organoid screen.

## Overview

The pipeline takes raw 5x brightfield images from the screen and:

A. Prepares the images (renaming, metadata, background correction)
B. Detects individual organoids with a custom-trained **Cellpose** model
C. Classifies organoids and measures the **ridge thickness** of cystic organoids
D. Incorporates manual corrections and exports per-image results

## Workflow

The scripts are numbered and **must be run in order**.

| Step | Script | Purpose |
|------|--------|---------|
| 0 | `0-Background correct and flatten, 0- Rename images and add metadata` | Renames images based on the input folder structure, adds metadata, and applies background correction. |
| 1 | `1 - Set Channel Names and Colors` | Sets the brightfield channel information. |
| 2 | `2 - Run Cellpose prediction` | Detects organoids in the brightfield images using the custom Cellpose model. |
| 3 | `3 - Convert detections to annotations` | Converts the organoid detections into annotations, so they can be manually reviewed and edited. |
| 4 | `4 - Check Thickness` | Measures the ridge thickness of organoids classified as cystic. |
| 5 | `5 - Add manual thickness measurement and objects` | Adds manual corrections and computes the final statistics. Only needed if manual annotations were made **after** running step 4. |
| 6 | `6 - Create Empty objects in images with nothing annotated` | Ensures that images with no annotations or detections still get a row in the exported results, so no image is silently dropped from the dataset. |

### Cellpose model

Organoid detection relies on a custom Cellpose model trained on 5x brightfield organoid images.
The reference of the model is DOI:10.5281/zenodo.23230322

## License
# add


