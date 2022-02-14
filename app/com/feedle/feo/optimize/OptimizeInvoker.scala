package com.feedle.feo.optimize

import com.feedle.feo.optimize.csssprite.CssSpriteInvoker
import com.feedle.feo.optimize.merger.MergeInvoker
import com.feedle.feo.optimize.replace.{Replace, ReplaceInvoker}
import com.feedle.feo.weboject.HtmlObject
import models._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._


/**
 * Created by infinitu on 2014. 6. 13..
 */
object OptimizeInvoker {
  def reoptimize(projectid:Int, html:HtmlObject){
    import com.feedle.feo.util.OptimizeType

    DB.withSession{implicit session=>

      ReplaceRules.tQuery.filter(_.projectId===projectid).list().foreach { rule =>
        ReplaceInvoker.optimize(rule)(html)
      }
      OptimizeRules.tQuery.filter(_.projectId===projectid).list().foreach{rule=>
        OptimizeType.DBValue2OptimizeType(rule.optimizeType) match{
          case OptimizeType.CssSprite=>
            CssSpriteInvoker.optimize(rule)(html)
            Replace.applyCssChanges(rule.optimizeId)(html)
          case OptimizeType.CssSpriteReverse=>
            CssSpriteInvoker.deoptimize(rule)(html)
            Replace.applyCssChanges(rule.optimizeId)(html)
            //todo replace => image / cdn(맨 아래에 위치)함으로 바꿨는데 맞겠지!!!!!
          case OptimizeType.Image=>
            ReplaceInvoker.optimize(rule)(html)
          case OptimizeType.ImageReverse=>

          case OptimizeType.Merge=>
            MergeInvoker.optimize(rule)(html)
          case OptimizeType.MergeRevserse=>
            MergeInvoker.deoptimize(rule)(html)
          case OptimizeType.Minify=>
            ReplaceInvoker.optimize(rule)(html)
          case OptimizeType.MinifyReverse=>

          case OptimizeType.Cdn=>
            ReplaceInvoker.optimize(rule)(html)
          case OptimizeType.CdnReverse=>
        }

      }
    }
  }
}
