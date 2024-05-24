import org.apache.commons.math3.fitting.WeightedObservedPoints;
//
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
//
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
//
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

//import org.python.apache.commons.compress.utils.

public class MetrologyMap_WeightedObserverved_BufferedImage_XYShapeRenderer {
    static String[] filenames = new String[]{"SampleMetrologyTHK_v1.csv"
            , "SampleMetrologyTHK_v2.csv", "SampleMetrologyTHK_v3.csv", "SampleMetrologyTHK_v4.csv"};
    //    static String filename ="SampleMetrologyTHK_v4.csv";
    HashMap<String, List<Double>> XYCircleZ;
    HashMap<String, List<Double>> XYPixels;
    WeightedObservedPoints weightedPoints = new WeightedObservedPoints();
    List<ColorRange> colorRanges = new ArrayList<>();
    LookupPaintScale waferPaintScale;
    //
    Point origin = new Point(0, 0);
    /** name of plot title. */
    String chartTitle;
    /** name of file title. */
    String SaveTo;
    /** Max of x-axis. */
    double xMax;
    /** Min of x-axis. */
    double xMin;
    /** Max of y-axis. */
    double yMax;
    /** Min of y-axis. */
    double yMin;
    /** Max of z-axis. */
    double zMax;
    /** Min of z-axis. */
    double zMin;
//    /** calc xmax for radii. */
//    double radiusXMax;
//    /** calc ymax for radii. */
//    double radiusYMax;
//    /** calc xmin for radii. */
//    double waferRadius;
    /** calc xmin in Pixel for radii. */
    double waferPixelRadius;
    double waferPixelRadiusY;
    double waferPixelRadiusX;
    /** Center x-axis. */
    double xOriginDataSpace;
    /** Center y-axis. */
    double yOriginDataSpace;
//    /** Ratio of Resolution to Wafer Dimension. */
//    protected double waferGraphRatio;
    static int plotPow = 6;
    static final int[] resolution = {1100, 900};
    /** Number of x intervals. */
    static final int numX = resolution[0];
    /** Number of y intervals. */
    static final int numY = resolution[1];
    static List<Double> XClist;
    static List<Double> YClist;
    static List<Double> ZVlist;
    static List<Double> XClistPixel;
    static List<Double> YClistPixel;
    String fileExtension;
    //
    public static class ColorRange {
        int min;
        int max;
        Color color;
        public ColorRange(int min, int max, Color color) {
            this.min = min;
            this.max = max;
            this.color = color;
        }
        public int getMin() {
            return min;
        }
//        public void setMin(int min) {
//            this.min = min;
//        }
        public int getMax() {
            return max;
        }
    }
    /**
     *This constructor takes 2 inputs.
     * @param title the frame title.
     * @param XYZ hashmap values
     */
    MetrologyMap_WeightedObserverved_BufferedImage_XYShapeRenderer(String title, String fileTitle
            , HashMap<String, List<Double>> XYZ, HashMap<String, List<Double>> XYZPixel, int pow) throws IOException {
        this.chartTitle = title;
        this.SaveTo = fileTitle;
        //fileExtension = ext;
        //System.out.println("what is fileExtension: " + fileExtension);
        this.XYCircleZ = XYZ;
        this.XYPixels = XYZPixel;
        //
        this.plotPow = pow;
        //
        this.xMin = CalcMaxMin.calcMin(this.XYCircleZ.get("X"));
        this.xMax = CalcMaxMin.calcMax(this.XYCircleZ.get("X"));
        this.yMin = CalcMaxMin.calcMin(this.XYCircleZ.get("Y"));
        this.yMax = CalcMaxMin.calcMax(this.XYCircleZ.get("Y"));
        //
        this.zMin = CalcMaxMin.calcMin(this.XYCircleZ.get("Z"));
        this.zMax = CalcMaxMin.calcMax(this.XYCircleZ.get("Z"));
        //
//        this.radiusXMax = calcAbsMax(this.XYCircleZ.get("X"));
//        this.radiusYMax = calcAbsMax(this.XYCircleZ.get("Y"));
//        this.waferRadius = Math.min(this.radiusXMax, this.radiusYMax);
//        this.waferPixelRadius = Math.min(numX / 2D, numY / 2D);
        this.waferPixelRadiusX = numX / 2D;
        this.waferPixelRadiusY = numY / 2D;
        //
        System.out.println("origin.x: " + origin.x);
        this.xOriginDataSpace = translateToPixel("x", origin.getX(),  xMax, xMin, numX);
        System.out.println("this.xOriginDataSpace: " + this.xOriginDataSpace);
        this.yOriginDataSpace = translateToPixel("y", origin.getY(), yMax, yMin, numY);
        System.out.println("this.yOriginDataSpace: " + this.yOriginDataSpace);
        //
//        this.waferGraphRatio = this.waferRadius * 2 / (Math.max(numX, numY));
        //
        this.waferPaintScale = new LookupPaintScale(this.zMin, this.zMax, Color.gray);
        //
        createChart(createDataset());
    }
    /**
     * Creates a dataset.
     * @return A dataset.
     */
    XYZDataset createDataset() {
        List<Double> XCoordsList = XYCircleZ.get("X");
        List<Double> YCoordsList = XYCircleZ.get("Y");
        List<Double> ZValsList = XYCircleZ.get("Z");
        return new XYZDataset() {
            public int getSeriesCount() {
                return 1;
            }
            public int getItemCount(int series) {
                return XCoordsList.size();
            }
            public Number getX(int series, int item) {
                return new Double(getXValue(series, item));
            }
            public double getXValue(int series, int item) {
                return XCoordsList.get(item);
            }
            public Number getY(int series, int item) {
                return new Double(getYValue(series, item));
            }
            public double getYValue(int series, int item) {
                return YCoordsList.get(item);
            }
            public Number getZ(int series, int item) {
                return new Double(getZValue(series, item));
            }
            public double getZValue(int series, int item) {
                return ZValsList.get(item);
            }
            public void addChangeListener(DatasetChangeListener listener) {
                // Required to inlcude the line
            }
            public void removeChangeListener(DatasetChangeListener listener) {
                // Required to inlcude the line
            }
            public DatasetGroup getGroup() {
                return null;
            }
            public void setGroup(DatasetGroup group) {
                // Required
            }
            public Comparable getSeriesKey(int series) {
                // Required
                return "XYZRenderer_230912";
            }
            public int indexOf(Comparable seriesKey) {
                return 0;
            }
            public DomainOrder getDomainOrder() {
                return DomainOrder.ASCENDING;
            }
        };
    }
    /**
     * Creates a sample chart.
     * @param dataset  the dataset.
     * @return A sample chart.
     */
    void createChart(XYZDataset dataset) throws IOException {
//        private JFreeChart createChart(XYZDataset dataset) throws IOException {
        NumberAxis numberaxisX = new NumberAxis("mm");
        numberaxisX.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxisX.setLowerMargin(0.0D);
        numberaxisX.setUpperMargin(0.0D);
        numberaxisX.setAxisLinePaint(Color.white);
        numberaxisX.setTickMarkPaint(Color.white);
        //
        NumberAxis numberaxisY = new NumberAxis("mm");
        numberaxisY.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxisY.setLowerMargin(0.0D);
        numberaxisY.setUpperMargin(0.0D);
        numberaxisY.setAxisLinePaint(Color.white);
        numberaxisY.setTickMarkPaint(Color.white);
        //
        NumberAxis numberaxisZ = new NumberAxis("Scale");
        numberaxisZ.setAxisLinePaint(Color.white);
        numberaxisZ.setTickMarkPaint(Color.white);
        numberaxisZ.setTickLabelFont(new Font("Dialog", 0, 7));

        //create the buffered image
        BufferedImage buffimage = getImage(numX, numY, plotPow);
        // Instantiate XYShapeRenderer for plot
        XYShapeRenderer xyshaperenderer = new XYShapeRenderer();
        //LookupPaintScale waferPaintScale = new LookupPaintScale();
        xyshaperenderer.setPaintScale(this.waferPaintScale);
        xyshaperenderer.findDomainBounds(dataset);
        xyshaperenderer.findRangeBounds(dataset);
        xyshaperenderer.findZBounds(dataset);

        // Initialize the XYPlot
        XYPlot xyplot = new XYPlot(dataset, numberaxisX, numberaxisY, xyshaperenderer);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setBackgroundAlpha((float) 0.25);
//        xyplot.setDomainGridlinesVisible(false);
        xyplot.setDomainGridlinesVisible(true);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setOutlinePaint(Color.blue);
        //
        // This gets the FULL heatmap in but too small...
        // need to translate buff image size to resolution...
//        XYDataImageAnnotation xydataimageannotation = new XYDataImageAnnotation(buffimage
//                , this.xOriginDataSpace * this.waferGraphRatio
//                , this.yOriginDataSpace * this.waferGraphRatio
//                , buffimage.getWidth() * this.waferGraphRatio
//                , buffimage.getHeight() * this.waferGraphRatio
//        );
        xyplot.setBackgroundImage(buffimage);
//        xyplot.drawBackgroundImage(buffimage.createGraphics(), new Rectangle(
//                (int) ((int) -this.xOriginDataSpace * this.waferGraphRatio)
//                , (int) ((int) -this.yOriginDataSpace * this.waferGraphRatio)
//                , (int) ((int) buffimage.getWidth() * this.waferGraphRatio)
//                , (int) ((int) buffimage.getHeight() * this.waferGraphRatio))
//        );
        xyplot.setBackgroundImageAlpha(1.0f);

        // Create Chart
        JFreeChart chart = new JFreeChart(this.chartTitle, xyplot);
        chart.removeLegend();
        //
        PaintScaleLegend waferPaintScaleLegend = new PaintScaleLegend(this.waferPaintScale, numberaxisZ);
        waferPaintScaleLegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        waferPaintScaleLegend.setFrame(new BlockBorder(Color.white));
        waferPaintScaleLegend.setStripWidth(50D);
        waferPaintScaleLegend.setPosition(RectangleEdge.LEFT);
//        waferPaintScaleLegend.setBackgroundPaint(new Color(120, 120, 180));
        waferPaintScaleLegend.setBackgroundPaint(Color.white);
        chart.addSubtitle(waferPaintScaleLegend);
//        chart.setBackgroundPaint(new Color(180, 198, 250));
        chart.setBackgroundPaint(Color.white);
        //
        // Add annotations for labeled points
        addDataPointAnnotations(xyplot);
        //
        // Display the chart in a frame
        JFrame frame = new JFrame(this.chartTitle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ChartPanel chartPanel = new ChartPanel(chart);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);

        /*export chart*/
        // This is for PNG
        ChartUtils.applyCurrentTheme(chart);
        File pngSaveTo = new File(SaveTo+".png");
        System.out.println("what was the file: " + pngSaveTo);
        System.out.println("what is numX " + numX);
        System.out.println("what is numY " + numY);
        ChartUtils.saveChartAsPNG(pngSaveTo, chart, numX, numY);

        // This is for SVG, file size TOO LARGE
        File svgSaveTo = new File(SaveTo+".svg");
//        exportChartAsSVG(chart, new Rectangle(0, 0, numX, numY), svgSaveTo);
//        exportChartAsSVG2(chart, svgSaveTo);
        exportChartAsSVG3(chart, svgSaveTo);
        //return chart;
    }
    //
    BufferedImage getImage(int width, int height, int pow) {
        System.out.println("getImage called");
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        initialState();
        System.out.println("initialState() is done");
        long startTime5 = System.currentTimeMillis();
        colorImage(bufferedImage, pow);
        long endTime5 = System.currentTimeMillis();
        System.out.println("Color lookup method\ntook:" + (endTime5 - startTime5) + " milliseconds");
        return bufferedImage;
    }
    //
    void initialState() {
        for (int i=0; i < this.XYPixels.get("X").size(); i++) {
            this.weightedPoints.add(this.XYCircleZ.get("Z").get(i), this.XYPixels.get("X").get(i), this.XYPixels.get("Y").get(i));
        }
        waferPaintScaleM();
    }
    //
    void waferPaintScaleM() {
        System.out.println("waferPaintScaleM is called");
        //
        int cR = 128;
        int cG = 0;
        int cB = 255;
        // determine loop Z val increment amount from # of RGB increments
        double zScaleVal = this.zMin;
        int cDelta = 0;
        cDelta = cDelta + (cR - 0) + (cG - 0) + (cB - 0);
        cDelta = cDelta + 255 * 3;
        //
        double zIncrement = (zMax - zMin) / cDelta;
        //
        int cIncrement = 1;
        boolean cRInc = false;
        boolean cGInc = false;
        boolean cBInc = true;
        //for (int c=0; c < this.cBlock; c++) {
        while (zScaleVal < this.zMax) {
            if ((cR > 0) && (!cRInc) && (!cGInc) && (cB == 255)) {
                if ((cR == 128) && (cG == 0) && (cB == 255)) {
                    this.colorRanges.add(new ColorRange(0, (int) this.zMin, new Color(cR, cG, cB)));
                } else {
                    this.colorRanges.add(new ColorRange((int) zScaleVal, (int) Math.min(zScaleVal + zIncrement, zMax)
                            , new Color(cR, cG, cB)));
                }
                this.waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, this.zMax);
                cR = Math.max(cR - cIncrement, 0);
                //System.out.println(colorRanges.get(0).color.getRGB());
                //System.out.println(colorRanges.get(0).color);
                //System.out.println("1st block");
            } else if ((cR == 0) && (cBInc) && (cB == 255) && (cG < 255)) {
                this.colorRanges.add(new ColorRange((int) zScaleVal, (int) Math.min(zScaleVal + zIncrement, zMax)
                        , new Color(cR, cG, cB)));
                this.waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, this.zMax);
                cG = Math.min(cG + cIncrement, 255);
                //System.out.printn("2nd block");
            } else if ((!cGInc) && (cBInc) && (!cRInc)) {
                cGInc = true;
                cG = Math.max(cG, 255);
                //System.out.println("3rd block");
            } else if ((cB > 0) && (cGInc) && (!cRInc)) {
                this.colorRanges.add(new ColorRange((int) zScaleVal, (int) Math.min(zScaleVal + zIncrement, zMax)
                        , new Color(cR, cG, cB)));
                this.waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, this.zMax);
                cB = Math.max(cB - cIncrement, 0);
                //System.out.println("4th block");
            } else if ((cB == 0) && (cBInc) && (!cRInc)) {
                cBInc = false;
                //System.out.println("5th block");
            } else if ((cR < 255) && (!cBInc) && (cGInc) && (!cRInc)) {
                this.colorRanges.add(new ColorRange((int) zScaleVal, (int) Math.min(zScaleVal + zIncrement, zMax)
                        , new Color(cR, cG, cB)));
                this.waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = zScaleVal + zIncrement;
                cR = Math.min(cR + cIncrement, 255);
                //System.out.println("6th block");
            } else if ((cR == 255) && (cGInc) && (!cRInc)) {
                cGInc = false;
                cRInc = true;
                //System.out.println("7th block");
            } else if ((cG > 0) && (cRInc) && (!cGInc) && (!cBInc)) {
                this.colorRanges.add(new ColorRange((int) zScaleVal, (int) Math.min(zScaleVal + zIncrement, zMax)
                        , new Color(cR, cG, cB)));
                this.waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, this.zMax);
                cG = Math.max(cG - cIncrement, 0);
                //System.out.println("2nd to last block");
            } else if ((cG == 0) && (cR == 255) && (cRInc) && (!cGInc) && (!cBInc)){
                this.colorRanges.add(new ColorRange((int) zScaleVal, (int) Math.min(zScaleVal + zIncrement, zMax)
                        , new Color(cR, cG, cB)));
                this.waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, this.zMax);
                //System.out.println("last block");
            }
        }
        // Add Max
        this.colorRanges.add(new ColorRange((int) this.zMax, (int) Double.POSITIVE_INFINITY
                , new Color(255, 0, 0)));
    }
    //
    void colorImage(BufferedImage bufferedImage, int pow) {
        double xOrigin = this.xOriginDataSpace;
        double yOrigin = this.yOriginDataSpace;
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
//              if (isPointWithinEllipse(i, j, xOrigin, yOrigin, this.waferPixelRadius)) {
                if (isPointWithinEllipse(i, j, xOrigin, yOrigin, this.waferPixelRadiusX, this.waferPixelRadiusY)) {
                    bufferedImage.setRGB(i, j, getValueShepard(i, j, pow));
                }
                else {
                    Color pixel = new Color(255, 255, 255);
                    int pixelRgb = pixel.getRGB();
                    bufferedImage.setRGB(i, j, pixelRgb);
                }
            }
        }
    }
    //
//    boolean isPointWithinEllipse(double x, double y, double centerX, double centerY, double xRradius) {
//        double dx = x - centerX;
//        double dy = y - centerY;
//        return ((dx * dx) / (radius * radius)) + ((dy * dy) / (radius * radius)) <= 1.0;
//    }
    //
    boolean isPointWithinEllipse(double x, double y, double centerX, double centerY, double xRadius, double yRadius) {
        double dx = x - centerX;
        double dy = y - centerY;
        return ((dx * dx) / (xRadius * xRadius)) + ((dy * dy) / (yRadius * yRadius)) <= 1.0;
    }
    //
    int getValueShepard(int i, int j, int pow) {
        double dTotal = 0.0;
        double result = 0.0;
        for (int p = 0; p < this.weightedPoints.toList().size(); p++) {
            double d = distance(this.weightedPoints.toList().get(p).getX() - this.origin.getX(),
                    this.weightedPoints.toList().get(p).getY() - this.origin.getY(), i, j);
//            if (pow != 1) {
//                d = Math.pow(d, pow);
//            }
            //d = Math.pow(d, pow-2);
            //d = Math.sqrt(d);
            //d = 1.0 / d;
            //
//            d = 1.0 / (Math.pow(d, 2));
            d = 1.0 / (Math.pow(d, pow));
//            if (d > 0.0) {
//                d = 1.0 / d;
//            } else { // if d is real small set the inverse to a large number
//                // to avoid INF
//                d = 1.e20;
//            }
//            System.out.println("d: " + d);
            result += this.weightedPoints.toList().get(p).getWeight() * d;
            dTotal += d;
        }
//        if (dTotal > 0) {
//            return getColor(result / dTotal);
//        } else {
//            System.out.println("in that else");
//            System.out.println("dTotal <= 0\n" + dTotal);
//            return getColor(255);
//        }
        Color pixelBlack = new Color(0, 0, 0);
        int pixelRgbBlack = pixelBlack.getRGB();
        Color pixelRed = new Color(255, 0, 0);
        int pixelRgbRed = pixelRed.getRGB();
        if (getColor(result / dTotal) == pixelRgbBlack) {
            System.out.println(" color black?");
            return pixelRgbRed;
        } else {
            return getColor(result / dTotal);
        }
    }
    //
    private int getColor(double val) {
        //for (ColorRange r : this.colorRanges) {
//        if (val == zMin) {
//            for (ColorRange r : colorRanges) {
//               System.out.println("this is r:\n" + r);
//               break;
//            }
//        }
        for (ColorRange r : colorRanges) {
            if (val >= r.min && val < r.max) {
//                System.out.println("r.min?\n" + r.min);
//                System.out.println("r.getMin?\n" + r.getMin());
//                System.out.println("r.max?\n" + r.max);
//                System.out.println("r.getMax?\n" + r.getMax());
                //System.out.println("can i check color: " + r.color);
                return r.color.getRGB();
            }
        }
        return 1;
    }
    /**
     * Calculates the distance between two points.
     * @param xDataPt *            the x coordinate.
     * @param yDataPt *            the y coordinate.
     * @param xGrdPt  *            the x grid coordinate.
     * @param yGrdPt  *            the y grid coordinate.
     * @return The distance between two points.
     */
    private double distance(double xDataPt, double yDataPt, double xGrdPt, double yGrdPt) {
        double dx = xDataPt - xGrdPt;
        double dy = yDataPt - yGrdPt;
        return Math.sqrt(dx * dx + dy * dy);
    }

    //
    void exportChartAsSVG3(JFreeChart chart, File svgFile) throws IOException {
        System.out.println("in exportChartAsSVG");
        System.out.println("File svgFile: " + svgFile);
        // Get a DOMImplementation and create an XML document
        // Create an instance of the SVG Generator
        SVGGraphics2D svg2d = new SVGGraphics2D(numX, numY);
        System.out.println("svgGenerator 'd");
        // draw the chart in the SVG generator
        chart.draw(svg2d, new Rectangle2D.Double(0, 0, numX, numY));
        // Write svg file
        SVGUtils.writeToSVG(svgFile, svg2d.getSVGElement());
    }
    // Add points for plot
    // private static void addDataPointAnnotations(ChartPanel chartPanel, XYPlot plot) {
    public void addDataPointAnnotations(XYPlot plot) {
        // msrmt labels for points
        String[] labels = new String[ZVlist.size()];
        for (int s = 0; s < ZVlist.size(); s++) {
            labels[s] = (ZVlist.get(s).toString());
        }
        // msrmnt X and Y coordinates of points
        double[] xValues = new double[ZVlist.size()];
        double[] yValues = new double[ZVlist.size()];
        for (int d = 0; d < ZVlist.size(); d++) {
            xValues[d] = XClist.get(d);
            yValues[d] = YClist.get(d);
        }
        // Font for the labels
        Font labelFont = new Font("SansSerif", Font.BOLD, 10);
        // Add annotations for each point
        for (int i = 0; i < labels.length; i++) {
            double x = xValues[i];
            double y = yValues[i];
            String label = labels[i];
            GeneralPath polygon = createPolygon(x, y, 0.5);
            plot.addAnnotation(new XYShapeAnnotation(polygon, new BasicStroke(1.5f), Color.BLACK));
//            plot.addAnnotation(new XYShapeAnnotation(polygon));
            if (i % 3 == 0) {
                //System.out.println("what is x\n"+x+"\nwhat is y\n"+y+"\nwhat is the label\n"+label);
                XYTextAnnotation annotation = new XYTextAnnotation(label, x, y);
                annotation.setFont(labelFont);
                annotation.setTextAnchor(TextAnchor.TOP_CENTER);
                plot.addAnnotation(annotation);
            }
        }
    }
    private GeneralPath createPolygon(double centerX, double centerY, double radius) {
        int sides = 9; // Number of sides of the polygon
        GeneralPath polygon = new GeneralPath();
        double angleIncrement = 2 * Math.PI / sides;

        double x = centerX + radius * Math.cos(0);
        double y = centerY + radius * Math.sin(0);
        polygon.moveTo(x, y);

        for (int i = 1; i <= sides; i++) {
            double angle = i * angleIncrement;
            x = centerX + radius * Math.cos(angle);
            y = centerY + radius * Math.sin(angle);
            polygon.lineTo(x, y);
//            polygon.contains(x, y);
        }
        polygon.closePath();
        return polygon;
    }
    //
    public static void Coords() {
        XClistPixel = new ArrayList<>();
        YClistPixel = new ArrayList<>();
        double tempXmax = CalcMaxMin.calcMax(XClist);
        double tempXmin = CalcMaxMin.calcMin(XClist);
        double tempYmax = CalcMaxMin.calcMax(YClist);
        double tempYmin = CalcMaxMin.calcMin(YClist);
//        System.out.println("the temp min/max list is\nxMax: " + tempXmax + "\nxMin: " + tempXmin
//                + "\nyMax: " + tempYmax + "\nyMin: " + tempYmin);
        for (int c=0; c < XClist.size(); c++){
            Double TempXClistPixel = translateToPixel("x", XClist.get(c), tempXmax, tempXmin, numX);
            Double TempYClistPixel = translateToPixel("y", YClist.get(c), tempYmax, tempYmin, numY);
            //
            XClistPixel.add(TempXClistPixel);
            YClistPixel.add(TempYClistPixel);
        }
    }
    public static Double translateToPixel(String coordType, double coordVal, double coordMax, double coordMin, int pixelFixedVal) {
        double TempCoordPixel = Double.POSITIVE_INFINITY;
        if (coordType.equals("x")){
            TempCoordPixel = (coordVal - coordMin) * (pixelFixedVal / (coordMax - coordMin));
        } else if (coordType.equals("y")) {
            TempCoordPixel = (pixelFixedVal - (coordVal - coordMin) * (pixelFixedVal / (coordMax - coordMin)));
        }
        return TempCoordPixel;
    }
    //
    public static void main(String[] args) throws IOException {
        for (String f : filenames) {
            long startTimeAll = System.currentTimeMillis();
            long startTime1 = System.currentTimeMillis();
            HashMap<String, HashMap<String, List<Double>>> MsrmntHashMap = MetrologyCSVReader.csvToHashMap(f);
            long endTime1 = System.currentTimeMillis();
            System.out.println("part1: the HashMap from CSV \ntook:" + (endTime1 - startTime1) + " milliseconds");
            //
            long startTime2 = System.currentTimeMillis();
            XClist = MsrmntHashMap.get("THK").get("X");
            YClist = MsrmntHashMap.get("THK").get("Y");
            ZVlist = MsrmntHashMap.get("THK").get("Z");
            long endTime2 = System.currentTimeMillis();
            System.out.println("part2: Making the X/Y/Z lists \ntook:" + (endTime2 - startTime2) + " milliseconds");
            //
            String ContourDatasetStyle = "BufferedImage";
            String JFreeChartStyle = "XYShapeRenderer";
            //String[] fileExts = {".png", ".svg"};
            //String fileExt = ".png";

            //Create new Pixel Data
            long startTime3 = System.currentTimeMillis();
            HashMap<String, List<Double>> XYPixelHash = new HashMap<String, List<Double>>();
            Coords();
            XYPixelHash.put("X", XClistPixel);
            XYPixelHash.put("Y", YClistPixel);
            long endTime3 = System.currentTimeMillis();
            System.out.println("part2: Creating the Hash Data \ntook:" + (endTime3 - startTime3) + " milliseconds");
            //
            int plotPower = 6;
            StringBuilder PlotSaveToDestination = new StringBuilder();
            String WaferMapTitle = "WaferTHK_" + JFreeChartStyle +
                    "_" + ContourDatasetStyle +
                    "_BufferPower:" + plotPow;
            //
            String filenameTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
                    .format(new Date());
            String mainFolder = "GeneratedMaps\\";
            //
            File directory = new File(mainFolder);
            if (!directory.exists()) {
                directory.mkdir();
            }
            //
            PlotSaveToDestination.append(mainFolder);
            PlotSaveToDestination.append("WaferTHK_").append(JFreeChartStyle)
                    .append("_").append(ContourDatasetStyle)
                    .append("_BufferPower-")
                    .append(plotPower)
                    .append("_").append(filenameTimeStamp);
            //PlotSaveToDestination.append(fileExt);
            //
            long startTime4 = System.currentTimeMillis();
//        new MetrologyMap_WeightedObserverved_BufferedImage_XYShapeRenderer(WaferMapTitle, PlotSaveToDestination.toString()
//                , MsrmntHashMap.get("THK"), XYPixelHash, fileExt, plotPow);
            new MetrologyMap_WeightedObserverved_BufferedImage_XYShapeRenderer(WaferMapTitle, PlotSaveToDestination.toString()
                    , MsrmntHashMap.get("THK"), XYPixelHash, plotPower);
            long endTime4 = System.currentTimeMillis();
            System.out.println("part3: Make the Plot \ntook:" + (endTime4 - startTime4) + " milliseconds");
            long endTimeAll = System.currentTimeMillis();
            System.out.println("Total: The XYPlot with XYShape Renderer & BufferedImage-WeightedObservedPoints " +
                    "\ntook:" + (endTimeAll - startTimeAll) + " milliseconds");
        }
    }
}