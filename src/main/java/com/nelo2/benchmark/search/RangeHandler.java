package com.nelo2.benchmark.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.settings.Settings;

public class RangeHandler extends AbstractHandler<SearchRequestBuilder> {

	private long from = -1;
	private long to = -1;
	
	@Override
	String name() {
		return "range";
	}

	@Override
	void handle(Settings settings, SearchRequestBuilder builder) {
		Settings handlerSettings = settings.getByPrefix(".range");
		from = handlerSettings.getAsLong(".from", 0l);
		to = handlerSettings.getAsLong(".to", 0l);
	}

}
