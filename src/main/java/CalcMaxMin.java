import java.util.List;

public class CalcMaxMin {
    public double[] MaxRawData;
    public double[] MinRawData;

    //
    public double calcMax(double[] rawData) {
        int rawSize = rawData.length;
        double[] rawData2 = new double[rawSize];
        for (int i = 0; i < rawData.length; i++) {
            if (!Double.isNaN(rawData[i]))
                //rawData2.add(val);
                rawData2[i] = rawData[i];
        }
        if (rawData2.length == 0)
            System.out.print("this isn't working");

        int j = 0;

        for (int i = 1; i < rawData2.length; i++) {
            Double val2 = rawData2[i];
            if (val2 > rawData2[j])
                j = i;
        }
        double maxRawData = rawData2[j];
//        System.out.println("\nthe max is: " + maxRawData);
        return maxRawData;
    }
    //
    public double calcMax(List<Double> rawData) {
        int rawSize = rawData.size();
        double[] rawData2 = new double[rawSize];
        for (int i = 0; i < rawSize; i++) {
            if (!Double.isNaN(rawData.get(i)))
                rawData2[i] = rawData.get(i);
        }
        if (rawData2.length == 0)
            System.out.print("this isn't working");

        int j = 0;

        for (int i = 1; i < rawData2.length; i++) {
            Double val2 = rawData2[i];
            if (val2 > rawData2[j])
                j = i;
        }
        double maxRawData = rawData2[j];
//        System.out.println("\nthe max is: " + maxRawData);
        return maxRawData;
    }
    public double calcAbsMax(double[] rawData) {
        int rawSize = rawData.length;
        double[] rawData2 = new double[rawSize];
        for (int i = 0; i < rawData.length; i++) {
            if (!Double.isNaN(rawData[i]))
                //rawData2.add(val);
                rawData2[i] = Math.abs(rawData[i]);
        }
        if (rawData2.length == 0)
            System.out.print("this isn't working");

        int j = 0;

        for (int i = 1; i < rawData2.length; i++) {
            Double val2 = Math.abs(rawData2[i]);
            if (val2 > Math.abs(rawData2[j]))
                j = i;
        }
        double maxRawData = rawData2[j];
//        System.out.println("\nthe max is: " + maxRawData);
        return maxRawData;
    }
    //
    public double calcMin(double[] rawData) {
        int rawSize = rawData.length;
        double[] rawData2 = new double[rawSize];
        for (int i = 0; i < rawData.length; i++) {
            if (!Double.isNaN(rawData[i]))
                rawData2[i] = rawData[i];
        }
        if (rawData2.length == 0)
            System.out.print("this isn't working");
        int j = 0;
        //for (int i = 1; i < rawData2.size(); i++) {
        for (int i = 1; i < rawData2.length; i++) {
            Double val2 = rawData2[i];
            if (val2 < rawData2[j])
                j = i;
        }
        double minRawData = rawData2[j];
//        System.out.println("\nthe min is: " + minRawData);

        return minRawData;
    }
    //
    public double calcMin(List<Double> rawData) {
        int rawSize = rawData.size();
        double[] rawData2 = new double[rawSize];
        for (int i = 0; i < rawSize; i++) {
            if (!Double.isNaN(rawData.get(i)))
                rawData2[i] = rawData.get(i);
        }
        if (rawData2.length == 0)
            System.out.print("this isn't working");
        int j = 0;
        //for (int i = 1; i < rawData2.size(); i++) {
        for (int i = 1; i < rawData2.length; i++) {
            Double val2 = rawData2[i];
            if (val2 < rawData2[j])
                j = i;
        }
        double minRawData = rawData2[j];
//        System.out.println("\nthe min is: " + minRawData);
        return minRawData;
    }
    public double calcAbsMin(double[] rawData) {
        int rawSize = rawData.length;
        double[] rawData2 = new double[rawSize];
        for (int i = 0; i < rawData.length; i++) {
            if (!Double.isNaN(rawData[i]))
                rawData2[i] = Math.abs(rawData[i]);
        }
        if (rawData2.length == 0)
            System.out.print("this isn't working");
        int j = 0;
        //for (int i = 1; i < rawData2.size(); i++) {
        for (int i = 1; i < rawData2.length; i++) {
            Double val2 = Math.abs(rawData2[i]);
            if (val2 < Math.abs(rawData2[j]))
                j = i;
        }
        double minRawData = rawData2[j];
//        System.out.println("\nthe min is: " + minRawData);

        return minRawData;
    }
}
