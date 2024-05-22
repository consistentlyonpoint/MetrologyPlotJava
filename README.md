# MetrologyPlotJava
### Purpose:
Plot metrology data as wafer map via simulation of Real-Fab data collection metrology tool.

### Goal:
* Find fastest interpolation method.
* Balance speed with image quality.
* Supply customers with wafer map image with metrology data.

### Approach
* Starting point: (x, y, z) values
  - (x, y) coordinates + z values
  - z values represent metrology data
* Additional X, Y points generated (number of points is an input parameter to scenario).
* Z values generated per (X, Y) point via interpolation function.