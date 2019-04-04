package com.coremedia.blueprint.connectors.rss;

import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.invalidation.InvalidationResult;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class RssInvalidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(RssInvalidator.class);

  static InvalidationResult invalidate(@NonNull ConnectorContext context, @NonNull RssConnectorServiceImpl service) {
    InvalidationResult result = new InvalidationResult(context);
    List<String> oldTitles = service.getRootCategory(context).getItems().stream().map(ConnectorItem::getName).collect(Collectors.toList());
    SyndFeed feed = service.getFeed(context);
    if (feed != null) {
      List<SyndEntry> entries = feed.getEntries();
      int count = 0;
      for (SyndEntry entry : entries) {
        ConnectorItem item = service.createRssAsset(entry);
        if (!oldTitles.contains(item.getName())) {
          count++;
        }
      }

      if (count > 0) {
        RssConnectorCategory rootCategory = service.getRootCategory(true);
        result.addMessage("rss", rootCategory, Arrays.asList(rootCategory.getName(), count));
        result.addEntity(rootCategory);
      }
      LOGGER.debug("'" + context.getConnectionId() + "' invalidation found " + count + " new elements.");
    }

    return result;
  }
}
