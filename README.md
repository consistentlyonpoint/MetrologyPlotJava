# MetrologyPlotJava
### Purpose:
Plot metrology data as wafer map via simulation of Real-Fab data collection metrology tool.

## Goal:
* Find fastest interpolation method.
  * Balance speed with image quality.
* Supply customers with wafer map image with metrology data.

## Approach
* Starting point: (x, y, z) values
  - (x, y) coordinates + z values
  - z values represent metrology data
* Additional X, Y points generated (number of points is an input parameter to scenario).
* Z values generated per (X, Y) point via interpolation function.

### Interpolators
* Package: smile.interpolation
  * Class: KrigingInterpolation2D
    - Number points: 4356
    - Duration: **13 milliseconds**
  * Class: ShepardInterpolation2D
    - Number points: 8281
    - Duration: **16 milliseconds**
* Package: apache.commons.math3
  * Interface: MultivariateInterpolator -> Class: MicrosphereInterpolator
    - Number points: 3136
    - Duration: **41830 milliseconds**

### Results - Speed & Quality
* **Package: smile.interpolation**
  * Class: KrigingInterpolation2D
    * Quality:
      - Images @ interpolated pts <= 1296 unacceptable quality.
      - Images @ interpolated pts > 1296 acceptable quality.
    * Minimum Duration: (for quality image) 7 ms
  * Class: ShepardInterpolation2D
    * φ is φ(r) = r-p
    * Quality: Images @ p = 2 unacceptable quality.
    * Quality: Images @ p > 2 acceptable.
      - @ interpolated pts : [1296, 2116, 3136, 4356, 5776, 7396, 9216].
    * Minimum Duration: (for quality image)
      - @p=3: 5 ms
      - @p=4: 4 ms
* **Package: apache.commons.math3**
  * Interface: MultivariateInterpolator
  * Class: MicrosphereInterpolator
    ** Quality: All images acceptable interpolation.
      - @ interpolated pts : [1296, 2116, 3136, 4356, 5776, 7396, 9216].
    * Minimum Duration: 17203 ms
      - @ interpolated pt = 1296
<img src="https://github.com/consistentlyonpoint/MetrologyPlotJava/assets/108585008/d69ed4f3-7406-4431-9d4e-f17228ae816b" width=450/>


