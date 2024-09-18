package me.quickscythe.vanillaflux.utils.polls.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PieChartGenerator {

    public static void generatePieChart(String title, Map<String, Integer> data, String filePath) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                title,   // chart title
                dataset, // dataset
                false,    // include legend
                false,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        chart.setBackgroundPaint(null);
        plot.setBackgroundPaint(null);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
//        plot.setLabelGenerator(null);
        plot.setLabelPaint(new Color(255, 255, 255));
//        plot.setInteriorGap(0.04);



        try {
            ChartUtils.saveChartAsPNG(new File(filePath), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
