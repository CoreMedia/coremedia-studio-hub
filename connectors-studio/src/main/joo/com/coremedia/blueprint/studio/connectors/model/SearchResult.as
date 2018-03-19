package com.coremedia.blueprint.studio.connectors.model {
import com.coremedia.ui.data.impl.BeanImpl;

public class SearchResult extends BeanImpl {

  public function getTotal():int {
    return get(TOTAL);
  }

  public function setTotal(total:int) {
    set(TOTAL, total);
  }

  public function getHits():Array {
    return get(HITS);
  }

  public function setHits(hits:Array) {
    set(HITS, hits);
  }

  public function reset():void {
    set(TOTAL, 0);
    set(HITS, []);
  }

  public static const HITS:String = "hits";
  public static const TOTAL:String = "total";
}
}
