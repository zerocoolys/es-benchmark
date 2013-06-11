package com.nelo2.benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import com.nelo2.benchmark.benchmarks.HighLightSearchBenchmark;
import com.nelo2.benchmark.benchmarks.HistogramFacetSearchBenchmark;
import com.nelo2.benchmark.benchmarks.IndexBenchmark;
import com.nelo2.benchmark.benchmarks.QueryFilterFacetSearchBenchmark;
import com.nelo2.benchmark.benchmarks.TermsFacetSearchBenchmark;
import com.nelo2.benchmark.benchmarks.TermsRangeFacetSearchBenchmark;
import com.nelo2.benchmark.result.TemplateFile;

public class BenchmarkSuit {

	static List<Class<? extends Abstractbenchmark>> suitClz = new ArrayList<Class<? extends Abstractbenchmark>>();

	static {
		suitClz.add(IndexBenchmark.class);
		suitClz.add(TermsFacetSearchBenchmark.class);
		suitClz.add(QueryFilterFacetSearchBenchmark.class);
		suitClz.add(HistogramFacetSearchBenchmark.class);
		suitClz.add(HighLightSearchBenchmark.class);
		suitClz.add(TermsRangeFacetSearchBenchmark.class);
	}

	public static void main(String[] args) {

		final Settings settings = ImmutableSettings.settingsBuilder().loadFromClasspath("benchmark.yml").build();
		BenchmarkClients clients = new BenchmarkClients(settings);

//		final CountDownLatch latch = new CountDownLatch(clients.length());

		ExecutorService service = Executors.newCachedThreadPool();
		for (final Client client : clients) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					try {
						JvmMonitor monitor = null;
						try {
							monitor = new JvmMonitor(settings, client);
							if (!monitor.start()) {
								return;
							}

							for (Class<? extends Abstractbenchmark> clz : suitClz) {
								Abstractbenchmark benchmark = clz.getConstructor(Settings.class,Client.class).newInstance(settings,client);
								benchmark.benchmark();
								resultString.put(benchmark.name(), benchmark.getResult());
								Thread.sleep(1000);
							}
							System.out.println("===================== result ===================");
							monitor.end();
							//			resultString.put(monitor.name(), monitor.getResult());
							StringBuilder output = new StringBuilder();
							output.append("<div id=\"modules\" class=\"accordion\">");
							Map<String, String> orders = new TreeMap<String, String>(settings.getAsMap());
							StringBuilder settingsStr = new StringBuilder();
							for (String key : orders.keySet()) {
								settingsStr.append(key).append("=").append(orders.get(key)).append("<br>");
							}
							appendAccordionGroup(output, "modules", "benchmark_conf",
									"<h2>Benchmark configuration</h2>", settingsStr.toString());
							appendAccordionGroup(output, "modules", "monitor", "<h2>" + monitor.name() + "</h2>",
									monitor.getResult());

							//			output.append("<h2>").append(monitor.name()).append("</h2>").append(monitor.getResult());
							outputHTML(output);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							cleanup(monitor);
						}
					} finally {
					}
				}
			});
		}


	}

	private static void cleanup(JvmMonitor monitor) {
		System.out.println(" output finished and cleanup..");
		monitor.cleanup();
		System.out.println(" cleanup success..");

	}

	static Map<String, String> resultString = new TreeMap<String, String>();

	private static void outputHTML(StringBuilder bodyHTML) {
		String filename = "result-" + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())
				+ ".html";
		filename = filename.replaceAll(":", "-").replaceAll(" ", "-");
		System.out.println("--> start to output the result:\n\t " + filename);

		for (Entry<String, String> entry : resultString.entrySet()) {
			appendAccordionGroup(bodyHTML, "modules", entry.getKey(), "<h2>" + entry.getKey() + "</h2>",
					entry.getValue());
		}
		bodyHTML.append("</div>");
		String templateHTML = TemplateFile.getTemplateString();
		templateHTML = templateHTML.replace("%module%", bodyHTML);
		File f = null;
		FileOutputStream fout = null;
		try {
			f = new File(filename);
			if (!f.exists()) {
				f.createNewFile();
			}
			fout = new FileOutputStream(f);
			fout.write(templateHTML.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fout = null;
			}
		}
	}

	public static void appendAccordionGroup(StringBuilder output, String pid, String id, String title, String content) {
		output.append("<div class=\"accordion-group\">").append("<div class=\"accordion-heading\">")
				.append("<a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#").append(pid)
				.append("\" href=\"#").append(id).append("\">").append(title).append("</a></div>").append("<div id=\"")
				.append(id).append("\" class=\"accordion-body collapse in\"><div class=\"accordion-inner\">")
				.append(content).append("</div></div></div>");
	}
}