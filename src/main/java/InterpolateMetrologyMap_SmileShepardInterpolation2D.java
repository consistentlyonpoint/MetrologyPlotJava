import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

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
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import smile.interpolation.ShepardInterpolation2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

//import org.python.apache.commons.compress.utils.

public class InterpolateMetrologyMap_SmileShepardInterpolation2D {
    protected static String[] filenames = new String[]{"SampleMetrologyTHK_v1.csv"
            , "SampleMetrologyTHK_v2.csv", "SampleMetrologyTHK_v3.csv", "SampleMetrologyTHK_v4.csv"};
//    protected static String filename1 ="SampleMetrologyTHK_v1.csv";
//    protected static String filename2 ="SampleMetrologyTHK_v2.csv";
//    protected static String filename3 ="SampleMetrologyTHK_v3.csv";
//    protected static String filename4 ="SampleMetrologyTHK_v4.csv";
    HashMap<String, List<Double>> XYZ;
    HashMap<String, List<Double>> XYCircleZ;
    /** name of plot title. */
    protected String chartTitle;
    /** name of file title. */
    protected String SaveTo;
    /** Max of x-axis. */
    protected double xMax;
    /** Min of x-axis. */
    protected double xMin;
    /** Max of y-axis. */
    protected double yMax;
    /** Min of y-axis. */
    protected double yMin;
    /** Max of z-axis. */
    protected double zMax;
    /** Min of z-axis. */
    protected double zMin;
    /** Size of render block height. */
    protected double zBlockH;
    /** Size of render block height. */
    protected double zBlockW;
    /** Increments of colorBlock. */
    protected double cBlock;
    /** calc xmax for radii. */
    protected double radiusXMax;
    /** calc xmin for radii. */
    protected double radiusMax;
    /** calc ymax for radii. */
    protected double radiusYMax;
    /** Center x-axis. */
    protected double centerX;
    /** Center y-axis. */
    protected double centerY;
    protected static int sqrtR = 105;
    protected int[] resolution = {1100, 900};
    /** Number of x intervals. */
    final int numX = resolution[0];//800; //resolution[0];
    /** Number of y intervals. */
    final int numY = resolution[1];
    protected static List<Double> XClist;
    protected static List<Double> YClist;
    protected static List<Double> ZVlist;
    protected static double[] XCoords;
    protected static double[] YCoords;
    protected static double[] ZVals;
    protected static double[] XCoordsSorted;
    protected static double[] YCoordsSorted;
    protected static double[] ZValsSorted;
    protected static double[][] XYCoords;
    protected static double[] XCoordsInterp;
    protected static double[] YCoordsInterp;
    protected static List<Double> ZValsDoubleInterp;
    protected static List<Double> XCoordsDoubleInterpFull;
    protected static List<Double> YCoordsDoubleInterpFull;
    protected static String fileExtension;
    protected static int interpolatedPointCount;
    protected static double ZInterp;
    //
    protected static double[] xyPoint;
    /**
     *This constructor takes 2 inputs.
     * @param title the frame title.
     * @param XYZ hashmap values
     * @param ptSq int
     */
    public InterpolateMetrologyMap_SmileShepardInterpolation2D(String title, String fileTitle, HashMap<String, List<Double>> XYZ, int ptSq, String ext)
            throws IOException {

        this.chartTitle = title;

        this.SaveTo = fileTitle;
        fileExtension = ext;
        //System.out.println("what is fileExtension: " + fileExtension);
        this.XYZ = XYZ;
        //
        this.xMin = CalcMaxMin.calcMin(this.XYZ.get("X"));
        this.xMax = CalcMaxMin.calcMax(this.XYZ.get("X"));
        this.yMin = CalcMaxMin.calcMin(this.XYZ.get("Y"));
        this.yMax = CalcMaxMin.calcMax(this.XYZ.get("Y"));
//        System.out.println("what is ymax and ymin\n" + yMax + "\n"+yMin);
        //
//        System.out.println("what is the type for the vals\n" + (this.XYZ.get("VALS")).getClass().getName());
        //
        this.centerX = 0;
        this.centerY = 0;
        this.radiusXMax = CalcMaxMin.calcAbsMax(this.XYZ.get("X"));
        this.radiusYMax = CalcMaxMin.calcAbsMax(this.XYZ.get("Y"));
        this.radiusXMax = Math.min(this.radiusXMax, this.radiusYMax);
        this.zMin = CalcMaxMin.calcMin(this.XYZ.get("Z"));
        this.zMax = CalcMaxMin.calcMax(this.XYZ.get("Z"));
        //
        this.zBlockW = Math.abs(xMax - xMin) / ptSq;
        this.zBlockH = Math.abs(yMax - yMin) / ptSq;
        this.cBlock = ptSq;
        // Before creating data, filter out points outside the circle.
        //this.XYCircleZ = filterDataByWafer(this.XYZ);
        this.XYCircleZ = filterDataByWafer();

        JPanel panel = new ChartPanel(createChart(createDataset()));
        panel.setPreferredSize(new Dimension(resolution[0], resolution[1]));
    }
    private HashMap<String, List<Double>> filterDataByWafer() {
        HashMap<String, List<Double>> XYZWafer = new HashMap<>();
        List<Double> filteredZValsDoubleInterp = new ArrayList<>();
        List<Double> filteredXCoordsDoubleInterpFull = new ArrayList<>();
        List<Double> filteredYCoordsDoubleInterpFull = new ArrayList<>();

        // Ellipse 1 center and radius
        double WaferCenterX = 0.0;
        double WaferCenterY = 0.0;
//        double WaferRadius = 150;
        double WaferRadius = radiusXMax;

        for (int i = 0; i < XYZ.get("X").size(); i++) {
            double x = XYZ.get("X").get(i);
            double y = XYZ.get("Y").get(i);
            double z = XYZ.get("Z").get(i);

            // Check if the point is within any of the ellipses
            if (isPointWithinEllipse(x, y, WaferCenterX, WaferCenterY, WaferRadius)) {
                filteredXCoordsDoubleInterpFull.add(x);
                filteredYCoordsDoubleInterpFull.add(y);
                filteredZValsDoubleInterp.add(z);
            }
        }
        //
        XYZWafer.put("X", filteredXCoordsDoubleInterpFull);
        XYZWafer.put("Y", filteredYCoordsDoubleInterpFull);
        XYZWafer.put("Z", filteredZValsDoubleInterp);
        return XYZWafer;
    }

    private static boolean isPointWithinEllipse(double x, double y, double centerX, double centerY, double radius) {
        double dx = x - centerX;
        double dy = y - centerY;
        return ((dx * dx) / (radius * radius)) + ((dy * dy) / (radius * radius)) <= 1.0;
    }
    /**
     * Creates a dataset.
     * @return A dataset.
     */
    protected XYZDataset createDataset() {
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
                return getXValue(series, item);
            }
            public double getXValue(int series, int item) {
                return XCoordsList.get(item);
            }
            public Number getY(int series, int item) {
                return getYValue(series, item);
            }
            public double getYValue(int series, int item) {
                return YCoordsList.get(item);
            }
            public Number getZ(int series, int item) {
                return getZValue(series, item);
            }
            public double getZValue(int series, int item) {
                return ZValsList.get(item);
            }
            public void addChangeListener(DatasetChangeListener listener) {
                // Required
            }
            public void removeChangeListener(DatasetChangeListener listener) {
                // Required
            }
            public DatasetGroup getGroup() {
                return null;
            }
            public void setGroup(DatasetGroup group) {
                // Required
            }
            public Comparable getSeriesKey(int series) {
                // Required
                return "XYZRenderer_230830";
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
    private JFreeChart createChart(XYZDataset dataset) throws IOException {
        NumberAxis numberaxisX = new NumberAxis("mm");
        numberaxisX.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxisX.setLowerMargin(0.0D);
        numberaxisX.setUpperMargin(0.0D);
        numberaxisX.setAxisLinePaint(Color.white);
        numberaxisX.setTickMarkPaint(Color.white);
        NumberAxis numberaxisY = new NumberAxis("mm");
        numberaxisY.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxisY.setLowerMargin(0.0D);
        numberaxisY.setUpperMargin(0.0D);
        numberaxisY.setAxisLinePaint(Color.white);
        numberaxisY.setTickMarkPaint(Color.white);

        // Instantiate xyblockRenderer for plot
        XYBlockRenderer xyblockrenderer = new XYBlockRenderer();
        //LookupPaintScale waferPaintScale = new LookupPaintScale();
        LookupPaintScale waferPaintScale = new LookupPaintScale(this.zMin, this.zMax, Color.gray);
        //cR as 128 for purple min
        int cR = 128;
        int cG = 0;
        int cB = 255;
        // determine loop Z val increment amount from # of RGB increments
        double zScaleVal = zMin;
        int cDelta = 0;
        cDelta = cDelta + (cR - 0) + (cG - 0) + (cB - 0);
        cDelta = cDelta + 255 * 3;
        //System.out.println("the color cDelta is " + cDelta);
        double zIncrement = (zMax - zMin) / cDelta;
        //System.out.println("the thk increment is " + zIncrement);
        //System.out.println("the zScal is " + zScaleVal);
        int cIncrement = 1;
        boolean cRInc = false;
        boolean cGInc = false;
        boolean cBInc = true;
        //for (int c=0; c < this.cBlock; c++) {
        while (zScaleVal < zMax) {
        /*
        System.out.println("the color cR is " + cR);
        System.out.println("the color cG is " + cG);
        System.out.println("the color cB is " + cB);
        System.out.println("the zValReached is " + zScaleVal);
         */
            if ((cR > 0) && (!cRInc) && (!cGInc) && (cB == 255)) {
                waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, zMax);
                cR = Math.max(cR - cIncrement, 0);
                //System.out.println("1st block");
            } else if ((cR == 0) && (cBInc) && (cB == 255) && (cG < 255)) {
                waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, zMax);
                cG = Math.min(cG + cIncrement, 255);
                //System.out.println("2nd block");
            } else if ((!cGInc) && (cBInc) && (!cRInc)) {
                cGInc = true;
                cG = Math.max(cG, 255);
                //System.out.println("3rd block");
            } else if ((cB > 0) && (cGInc) && (!cRInc)) {
                waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, zMax);
                cB = Math.max(cB - cIncrement, 0);
                //System.out.println("4th block");
            } else if ((cB == 0) && (cBInc) && (!cRInc)) {
                cBInc = false;
                //System.out.println("5th block");
            } else if ((cR < 255) && (!cBInc) && (cGInc) && (!cRInc)) {
                waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = zScaleVal + zIncrement;
                cR = Math.min(cR + cIncrement, 255);
                //System.out.println("6th block");
            } else if ((cR == 255) && (cGInc) && (!cRInc)) {
                cGInc = false;
                cRInc = true;
                //System.out.println("7th block");
            } else if ((cG > 0) && (cRInc) && (!cGInc) && (!cBInc)) {
                waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, zMax);
                cG = Math.max(cG - cIncrement, 0);
                //System.out.println("2nd to last block");
            } else if ((cG == 0) && (cR == 255) && (cRInc) && (!cGInc) && (!cBInc)){
                waferPaintScale.add(zScaleVal, new Color(cR, cG, cB));
                zScaleVal = Math.min(zScaleVal + zIncrement, zMax);
                //System.out.println("last block");
            }
        }
        xyblockrenderer.setPaintScale(waferPaintScale);
        xyblockrenderer.setBlockHeight(zBlockH);
        xyblockrenderer.setBlockWidth(zBlockW);
        //Graphics g = null;
        //Graphics2D g2D = (Graphics2D) g;
        //Graphics2D g2d = Component.getGraphics();
        //xyblockrenderer.drawItemLabel();

        // Initialize the XYPlot
        XYPlot xyplot = new XYPlot(dataset, numberaxisX, numberaxisY, xyblockrenderer);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setOutlinePaint(Color.blue);

        // Create Chart
        JFreeChart chart = new JFreeChart(chartTitle, xyplot);
        chart.removeLegend();
        NumberAxis numberaxisZ = new NumberAxis("Scale");
        numberaxisZ.setAxisLinePaint(Color.white);
        numberaxisZ.setTickMarkPaint(Color.white);
        numberaxisZ.setTickLabelFont(new Font("Dialog", 0, 7));
        PaintScaleLegend waferPaintScaleLegend = new PaintScaleLegend(waferPaintScale, numberaxisZ);
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
        /*
        ChartUtilities.applyCurrentTheme(chart);
        ChartUtilities.saveChartAsPNG(new File(SaveTo), chart, numX, numY);
        */
        if (fileExtension.equals(".png")) {
            ChartUtils.applyCurrentTheme(chart);
            ChartUtils.saveChartAsPNG(new File(SaveTo), chart, numX, numY);
            //System.out.println(".PNG - SaveTo\n" + SaveTo);
        } else {
            /*export as svg too?*/
            System.out.println(".SVG - SaveTo\n" + SaveTo);
            File SaveToFile = new File(SaveTo);
            exportChartAsSVG(chart, new Rectangle(0, 0, numX, numY), SaveToFile);
        }
        // Display the chart in a frame
        JFrame frame = new JFrame(chartTitle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ChartPanel chartPanel = new ChartPanel(chart);
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
        return chart;
    }
    void exportChartAsSVG(JFreeChart chart, Rectangle bounds, File svgFile) throws IOException {
        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        chart.draw(svgGenerator, bounds);

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        svgGenerator.stream(out, true /* use css */);
        outputStream.flush();
        outputStream.close();
    }
    // Add points for plot
    // private static void addDataPointAnnotations(ChartPanel chartPanel, XYPlot plot) {
    public static void addDataPointAnnotations(XYPlot plot) {
        // msrmt labels for points
        String[] labels = new String[ZVlist.size()];
        for (int s = 0; s < ZVlist.size(); s++) {
            //double tempLabel = Math.round(ZVlist.get(s) * 100) / 100;
            labels[s] = (ZVlist.get(s).toString());
            //labels[s] = String.valueOf(tempLabel);
            //labels[s] = tempLabel.toString();
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
            //double[] polygonPoint = {x, y};
            //XYPolygonAnnotation pointAnnotation = new XYPolygonAnnotation(polygonPoint);
            //plot.addAnnotation(pointAnnotation);
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
    private static GeneralPath createPolygon(double centerX, double centerY, double radius) {
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
    public static HashMap<String, List<Double>> MakeInterpArray() {
        HashMap<String, List<Double>> XYZHash = new HashMap<>();
        long startTime2a = System.currentTimeMillis();
        Coords();
        long endTime2a = System.currentTimeMillis();
        System.out.println("part2a: Format the Init Coords \ntook:" + (endTime2a - startTime2a) + " milliseconds");
        //
        long startTime2b = System.currentTimeMillis();
        SortArray();
        long endTime2b = System.currentTimeMillis();
        System.out.println("part2b: Sort the Init Coords \ntook:" + (endTime2b - startTime2b) + " milliseconds");
        //
        long startTime2c = System.currentTimeMillis();
        xArray();
        long endTime2c = System.currentTimeMillis();
        System.out.println("part2c: Create the X Coords \ntook:" + (endTime2c - startTime2c) + " milliseconds");
        //
        long startTime2d = System.currentTimeMillis();
        yArray();
        long endTime2d = System.currentTimeMillis();
        System.out.println("part2d: Create the Y Coords \ntook:" + (endTime2d - startTime2d) + " milliseconds");
        //
        long startTime2e = System.currentTimeMillis();
        interpolateXY();
        long endTime2e = System.currentTimeMillis();
        System.out.println("part2e: Create XYCoords for interp Z\ntook:" + (endTime2e - startTime2e) + " milliseconds");
        System.out.println("part2_c-e: Check the x/y and interp XY\ntook:" + (endTime2e - startTime2c) + " milliseconds");
        //
        long startTime2f = System.currentTimeMillis();
        //KrigingInterpolation2D krigingInterpolator = new KrigingInterpolation2D(XCoordsSorted, YCoordsSorted, ZValsSorted);
        ShepardInterpolation2D shepardInterpolator = new ShepardInterpolation2D(XCoordsSorted, YCoordsSorted, ZValsSorted, 4);
        //MultivariateInterpolator interpolator = new MicrosphereInterpolator();
        long endTime2f = System.currentTimeMillis();
        System.out.println("part2g: Create KrigingInterpolation2D interpolator\ntook:" + (endTime2f - startTime2f) + " milliseconds");
        //
        ZValsDoubleInterp = new ArrayList<>();
        XCoordsDoubleInterpFull = new ArrayList<>();
        YCoordsDoubleInterpFull = new ArrayList<>();
        //
        //ZValsInterp = new double[XYCoords.length];
        //XCoordsInterpFull = new double[XYCoords.length];
        //YCoordsInterpFull = new double[XYCoords.length];
        long startTime2g = System.currentTimeMillis();
        //for (int zi = 0; zi < ZValsInterp.length; zi++) {
        for (int zi = 0; zi < interpolatedPointCount; zi++) {
            double xpoint = XYCoords[zi][0];
            double ypoint = XYCoords[zi][1];
            //ZInterp = krigingInterpolator.interpolate(xpoint, ypoint);
            ZInterp = shepardInterpolator.interpolate(xpoint, ypoint);
            //XCoordsInterpFull[zi] = xpoint;
            //YCoordsInterpFull[zi] = ypoint;
            //
            //ZValsDoubleInterp.add(ZValsInterp[zi]);
            ZValsDoubleInterp.add(ZInterp);
            XCoordsDoubleInterpFull.add(xpoint);
            YCoordsDoubleInterpFull.add(ypoint);
        }
        long endTime2g = System.currentTimeMillis();
        System.out.println("part2i: Build XYZ Interp for loop\ntook:" + (endTime2g - startTime2g) + " milliseconds");
        //
        XYZHash.put("X", XCoordsDoubleInterpFull);
        XYZHash.put("Y", YCoordsDoubleInterpFull);
        XYZHash.put("Z", ZValsDoubleInterp);
        return XYZHash;
    }

    //
    public static void Coords() {
        XCoords = new double[XClist.size()];
        YCoords = new double[XClist.size()];
        ZVals = new double[XClist.size()];
        for (int c=0; c < XClist.size(); c++){
            XCoords[c] = XClist.get(c);
            YCoords[c] = YClist.get(c);
            ZVals[c] = ZVlist.get(c);
        }
    }
    public static void xArray() {
        double xMin = CalcMaxMin.calcMin(XCoords);
        double xMax = CalcMaxMin.calcMax(XCoords);
//        System.out.println("xCoords Max "+xMax+"\nxCoords Min "+xMin);
        double xIncrement = (xMax - xMin)/sqrtR;
        double currX = xMax * 1;
        XCoordsInterp = new double[sqrtR+1];
        for (int i = 0; i < sqrtR+1; i++) {
            currX = xMax - xIncrement * i;
            XCoordsInterp[i] = currX;
        }
        //System.out.println(Arrays.toString(XCoordsInterp));
    }
    // Linear-search function to find the index of an element
    /**
     * Method to find index of value in argument
     * @param arr double[]
     * @param t double
     * @return i index
     */
    public static int findIndex(double[] arr, double t) {
        // if array is Null
        if (arr == null) {
            return -1;
        }
        // find length of array
        int len = arr.length;
        int i = 0;
        // traverse in the array
        while (i < len) {
            // if the i-th element is t
            // then return the index
            if (arr[i] == t) {
                return i;
            }
            else {
                i = i + 1;
            }
        }
        return -1;
    }
    public static void SortArray() {
        XCoordsSorted = new double[XCoords.length];
        YCoordsSorted = new double[YCoords.length];
        ZValsSorted = new double[ZVals.length];
        //
        XCoordsSorted = XCoords.clone();
        Arrays.sort(XCoordsSorted);
        for (int xS=0; xS < XCoords.length; xS++) {
            int xIndex = findIndex(XCoords, XCoordsSorted[xS]);
            YCoordsSorted[xS] = YCoords[xIndex];
            ZValsSorted[xS] = (ZVals[xIndex]);
        }
    }
    public static void yArray() {
        double yMin = CalcMaxMin.calcMin(YCoords);
        double yMax = CalcMaxMin.calcMax(YCoords);
        double yIncrement = (yMax - yMin)/sqrtR;
        double currY = yMax * 1;
        YCoordsInterp = new double[sqrtR+1];
        for (int i = 0; i < sqrtR+1; i++) {
            currY = yMax - yIncrement * i;
            YCoordsInterp[i] = currY;
        }
        //System.out.println("YCoordsInterp.length\n" + YCoordsInterp.length);
    }
    public static void interpolateXY() {
        interpolatedPointCount = XCoordsInterp.length * YCoordsInterp.length;
        //XYCoords = new double[XCoordsInterp.length * YCoordsInterp.length][2];
        XYCoords = new double[interpolatedPointCount][2];
        int XYCoordsCount = 0;
        int xiMax = XCoordsInterp.length;
        int yiMax = YCoordsInterp.length;
        int xi = 0;
        int yi = 0;
        //double[] xyPoint = new double [XCoordsInterp.length * YCoordsInterp.length];
        while (XYCoordsCount < XCoordsInterp.length * YCoordsInterp.length) {
            if (xi < xiMax) {
                if (yi < yiMax) {
                    //double[] xyPoint = {XCoordsInterp[xi], YCoordsInterp[yi]};
                    xyPoint = new double[]{XCoordsInterp[xi], YCoordsInterp[yi]};
                    XYCoords[XYCoordsCount] = xyPoint;
                } else {
                    xi += 1;
                    yi = 0;
                    xyPoint = new double[]{XCoordsInterp[xi], YCoordsInterp[yi]};
                    XYCoords[XYCoordsCount] = xyPoint;
                }
                yi += 1;
                //System.out.println(Arrays.toString(xyPoint));
            }
            //System.out.println(Arrays.toString(xyPoint));
            XYCoordsCount += 1;
        }
        //System.out.println(Arrays.toString(XYCoords));
    }
    public static void main(String[] args) throws IOException {
        for (String f : filenames) {
            long startTimeAll = System.currentTimeMillis();
            long startTime1 = System.currentTimeMillis();
            HashMap<String, HashMap<String, List<Double>>> MsrmntHashMap = MetrologyCSVReader.csvToHashMap(f);
            long endTime1 = System.currentTimeMillis();
            System.out.println("part1: the HashMap from CSV \ntook:" + (endTime1 - startTime1) + " milliseconds");
            //
            //        System.out.println("this is the first data plot\n" + MsrmntHashMap);
            XClist = MsrmntHashMap.get("THK").get("X");
            YClist = MsrmntHashMap.get("THK").get("Y");
            ZVlist = MsrmntHashMap.get("THK").get("Z");
            String ContourDatasetStyle = "XYRenderer";
            //String[] fileExts = {".png", ".svg"};
            String fileExts = ".svg";

            //Create new points via interpolation
            long startTime2 = System.currentTimeMillis();
            HashMap<String, List<Double>> XYZHash = MakeInterpArray();
            long endTime2 = System.currentTimeMillis();
            System.out.println("part2: Creating the Interpolated Data \ntook:" + (endTime2 - startTime2) + " milliseconds");

            //for (String s : fileExts) {
            StringBuilder PlotSaveToDestination = new StringBuilder();
            String WaferMapTitle = "WaferTHK_XYZRender_Smile_ShepardInterpolation2D" + "_InterpPts:" + (int) Math.pow(sqrtR + 1, 2);
            //
            String filenameTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
                    .format(new Date());
//            String mainFolder = "C:\\Users\\tpizzone\\OneDrive - Eyelit Inc\\Code\\Java" +
//                    "\\SmileImplementation\\GenerateMap";
            String mainFolder = "GeneratedMaps\\";
//            String subFolder = "src\\main\\java";
            //String fileType = ".png";
//        String joinFolder = "\\";
            //
            File directory = new File(mainFolder);
            if (!directory.exists()) {
                directory.mkdir();
            }
            //
            PlotSaveToDestination.append(mainFolder);
//            PlotSaveToDestination.append(mainFolder).append(joinFolder).append(subFolder)
//                    .append(joinFolder);
            PlotSaveToDestination.append("WaferTHK_XYZRender_Smile_ShepardInterpolation2D").append(ContourDatasetStyle)
                    .append("_NumPts-")
                    .append((int) Math.pow(sqrtR + 1, 2))
                    .append("_").append(filenameTimeStamp);
            //            PlotSaveToDestination.append(fileType);
            //PlotSaveToDestination.append(s);
            PlotSaveToDestination.append(fileExts);
            //new InterpolateMetrologyMapClassic(WaferMapTitle, PlotSaveToDestination.toString(), XYZHash, sqrtR, s);
            //
            long startTime3 = System.currentTimeMillis();
            new InterpolateMetrologyMap_SmileShepardInterpolation2D(WaferMapTitle, PlotSaveToDestination.toString(), XYZHash, sqrtR, fileExts);
            long endTime3 = System.currentTimeMillis();
            System.out.println("part3: Make the Plot \ntook:" + (endTime3 - startTime3) + " milliseconds");
            long endTimeAll = System.currentTimeMillis();
            System.out.println("Total: The Smile-ShepardInterpolation2D WHOLE thing \ntook:" + (endTimeAll - startTimeAll) + " milliseconds");
        }
    }
}