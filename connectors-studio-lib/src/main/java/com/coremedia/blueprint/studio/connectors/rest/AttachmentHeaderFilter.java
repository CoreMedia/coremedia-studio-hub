package com.coremedia.blueprint.studio.connectors.rest;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AttachmentHeaderFilter implements Filter {
  public static final String URL_PATTERN = ";blob=";
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);

    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpServletRequest = (HttpServletRequest) request;
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;

      String contentDisposition = httpServletResponse.getHeader("Content-Disposition");
      String url = httpServletRequest.getRequestURI();
      if(url.contains(URL_PATTERN) && !StringUtils.isEmpty(contentDisposition) ) {
        if(contentDisposition.contains("filename") && !contentDisposition.contains("attachment")) {
          httpServletResponse.setHeader("Content-Disposition", "attachment; " + contentDisposition);
        }
      }
    }
  }

  @Override
  public void destroy() {

  }
}
