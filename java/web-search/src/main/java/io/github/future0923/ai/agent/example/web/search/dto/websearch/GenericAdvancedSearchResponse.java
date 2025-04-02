package io.github.future0923.ai.agent.example.web.search.dto.websearch;

import com.aliyun.tea.NameInMap;
import com.aliyun.tea.TeaModel;

import java.util.Map;

public class GenericAdvancedSearchResponse extends TeaModel {

	@NameInMap("headers")
	public Map<String, String> headers;

	@NameInMap("statusCode")
	public Integer statusCode;

	@NameInMap("body")
	public GenericSearchResult body;

	public GenericAdvancedSearchResponse() {
	}

	public static GenericAdvancedSearchResponse build(Map<String, ?> map) throws Exception {
		GenericAdvancedSearchResponse self = new GenericAdvancedSearchResponse();
		return (GenericAdvancedSearchResponse) TeaModel.build(map, self);
	}

	public GenericAdvancedSearchResponse setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public GenericAdvancedSearchResponse setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	public Integer getStatusCode() {
		return this.statusCode;
	}

	public GenericAdvancedSearchResponse setBody(GenericSearchResult body) {
		this.body = body;
		return this;
	}

	public GenericSearchResult getBody() {
		return this.body;
	}
}
