package me.quickscythe.vanillaflux.utils.polls.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ChartGenerator {

    public static void generatePieChart(String title, Map<String, Float> data, String filePath) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(title,   // chart title
                dataset, // dataset
                false,    // include legend
                false, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        styleChart(chart);
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(new Color(255, 255, 255));
        plot.setLabelFont(new Font("Arial", Font.BOLD, 24));


        try {
            ChartUtils.saveChartAsPNG(new File(filePath), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateBarChart(String title, Map<String, Float> data, String xAxis, String yAxis, String filePath) {
        generateBarChart(title, data, xAxis, yAxis, filePath, true);
    }

    public static void generateBarChart(String title, Map<String, Float> data, String xAxis, String yAxis, String filePath, boolean sort) {
        DefaultCategoryDataset dataset;
        if (sort)
            dataset = sortDataset(data);
        else {
            dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Float> entry : data.entrySet()) {
                dataset.addValue(entry.getValue(), "Votes", entry.getKey());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(title,                      // chart title
                xAxis,                      // x axis label
                yAxis,                      // y axis label
                dataset,                    // dataset
                PlotOrientation.HORIZONTAL, // orientation
                false,                      // include legend
                false,                      // tooltips
                false                       // urls
        );
        try {
            ChartUtils.saveChartAsPNG(new File(filePath), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(null);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 36));
        chart.getTitle().setPaint(new Color(255, 255, 255));
        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlinePaint(null);

    }

    private static DefaultCategoryDataset sortDataset(Map<String, Float> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
// Sort data by values in descending order
        ArrayList<Map.Entry<String, Float>> sortedData = new ArrayList<>(data.entrySet());
        sortedData.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Add sorted data to dataset
        for (Map.Entry<String, Float> entry : sortedData) {
            dataset.addValue(entry.getValue(), "sorted", entry.getKey());
        }

        return dataset;
    }

}
