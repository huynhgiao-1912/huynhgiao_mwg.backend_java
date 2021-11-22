package mwg.wb.webapi;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class SecondFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

//		HttpServletRequest req = (HttpServletRequest) request;
//		// HttpServletResponse resp = (HttpServletResponse)response;
//		// apiproduct/getproduct
//		switch (req.getRequestURI()) {
//		case "/apiproduct/getproduct":
//
//			chain.doFilter(request, response);
//			return;
//
//		}

		chain.doFilter(request, response);

		/*
		 * log.info("URL---->"+request.getProtocol());
		 * log.info("SERVERPort--->"+request.getServerPort());
		 * log.info("ContentType---->"+request.getContentType());
		 * log.info("ENCODING---->"+request.getCharacterEncoding());
		 * log.info("IP---->"+request.getRemoteAddr());
		 * log.info("HOST---->"+request.getRemoteHost());
		 * log.info("REMO PORT---->"+request.getRemotePort());
		 */
	}

	@Override
	public void destroy() {

	}
}