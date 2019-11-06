package com.coremedia.blueprint.studio.connectors.actions {
import com.coremedia.cap.common.Job;
import com.coremedia.cap.common.JobContext;
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.collectionview.CollectionViewConstants;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.jobs.BackgroundJob;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;

import flash.utils.setTimeout;

import mx.resources.ResourceManager;

public class FakeJob implements Job, BackgroundJob {

  protected var jobContext:JobContext;

  public function FakeJob() {
  }

  public function execute(jobContext:JobContext):void {
    this.jobContext = jobContext;
    setTimeout(function ():void {
      jobContext.notifyProgress(0.2);
    }, 1000);
    setTimeout(function ():void {
      jobContext.notifyProgress(0.4);
    }, 2000);
    setTimeout(function ():void {
      jobContext.notifyProgress(0.6);
    }, 3000);
    setTimeout(function ():void {
      jobContext.notifyProgress(0.8);
    }, 4000);
    setTimeout(function ():void {
      jobContext.notifySuccess({});
    }, 5000);
  }

  public function requestAbort(jobContext:JobContext):void {
    jobContext.notifyAbort();
  }

  public function getNameExpression():ValueExpression {
    return ValueExpressionFactory.createFromValue(ResourceManager.getInstance().getString('com.coremedia.blueprint.studio.connectors.ConnectorsStudioPlugin', 'Action_FakeImport_Job_text'));
  }

  public function getIconClsExpression():ValueExpression {
    return null;
  }

  public function getErrorHandler():Function {
    return null;
  }

  public function getSuccessHandler():Function {
    var content:Content = editorContext.getSession().getConnection().getContentRepository().getChild("/Sites/Calista/United States/English/Editorial/Content/Summer Fashion Campaign Looks/");
    var contentTypeName:String = content.getType().getName();

    return function ():void {
      editorContext.getCollectionViewExtender().getContentTreeRelation(contentTypeName).showInTree([content], CollectionViewConstants.THUMBNAILS_VIEW);
    }
  }
}
}
