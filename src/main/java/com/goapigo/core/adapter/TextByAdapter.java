package com.goapigo.core.adapter;

import com.goapigo.core.annotations.TextBy;
import com.goapigo.core.exception.ElementParsingException;
import java.lang.annotation.Annotation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
@RequiredArgsConstructor
public class TextByAdapter implements Adaptable<Object> {

  private final Annotation annotation;
  private final String htmlContent;
  private final Class<? extends Object> responseClass;

  public Object adapt() {
    TextBy textBy = (TextBy) annotation;
    String cssSelector = textBy.value();
    Document document = Jsoup.parse(htmlContent);
    Elements elements = document.select(cssSelector);
    return elements.stream()
        .filter(this::isParseable)
        .map(this::elementToObject)
        .findFirst()
        .orElse(null);
  }

  private Object elementToObject(Element element) {
    try {
      if (isResponseString() || responseClass.getConstructor(String.class) == null) {
        return element.text();
      }
      return responseClass.getConstructor(String.class).newInstance(element.text());
    } catch (Exception e) {
      throw new ElementParsingException(e);
    }
  }

  private boolean isResponseString() {
    return responseClass.isAssignableFrom(String.class);
  }

  private boolean isParseable(Element element) {
    try {
      return responseClass.getConstructor(String.class) != null || isResponseString();
    } catch (NoSuchMethodException e) {
      log.error("No constructor found.", e);
      return false;
    }
  }
}
