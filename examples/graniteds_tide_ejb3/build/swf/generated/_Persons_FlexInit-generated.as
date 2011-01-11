package {
import flash.utils.*;
import mx.core.IFlexModuleFactory;
import flash.system.*
import flash.net.registerClassAlias;
import flash.net.getClassByAlias;
import mx.collections.ArrayCollection;
import mx.collections.ArrayList;
import mx.messaging.config.ConfigMap;
import mx.messaging.messages.AcknowledgeMessage;
import mx.messaging.messages.AcknowledgeMessageExt;
import mx.messaging.messages.AsyncMessage;
import mx.messaging.messages.AsyncMessageExt;
import mx.messaging.messages.CommandMessage;
import mx.messaging.messages.CommandMessageExt;
import mx.messaging.messages.ErrorMessage;
import mx.messaging.messages.MessagePerformanceInfo;
import mx.messaging.messages.RemotingMessage;
import mx.utils.ObjectProxy;
import org.granite.collections.BasicMap;
import org.granite.collections.UIDList;
import org.granite.collections.UIDSet;
import org.granite.math.BigDecimal;
import org.granite.math.BigInteger;
import org.granite.math.Long;
import org.granite.math.MathContext;
import org.granite.math.RoundingMode;
import org.granite.persistence.PersistentBag;
import org.granite.persistence.PersistentList;
import org.granite.persistence.PersistentMap;
import org.granite.persistence.PersistentSet;
import org.granite.tide.TideMessage;
import org.granite.tide.invocation.ContextEvent;
import org.granite.tide.invocation.ContextEventListener;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.validators.InvalidValue;
import test.granite.ejb3.entity.AbstractEntity;
import test.granite.ejb3.entity.Address;
import test.granite.ejb3.entity.Contact;
import test.granite.ejb3.entity.Country;
import test.granite.ejb3.entity.Person;
import test.granite.ejb3.entity.Person$Salutation;
import test.granite.ejb3.service.PersonService;
import mx.effects.EffectManager;
import mx.core.mx_internal;

[Mixin]
public class _Persons_FlexInit
{
   public function _Persons_FlexInit()
   {
       super();
   }
   public static function init(fbs:IFlexModuleFactory):void
   {
      EffectManager.mx_internal::registerEffectTrigger("addedEffect", "added");
      EffectManager.mx_internal::registerEffectTrigger("creationCompleteEffect", "creationComplete");
      EffectManager.mx_internal::registerEffectTrigger("focusInEffect", "focusIn");
      EffectManager.mx_internal::registerEffectTrigger("focusOutEffect", "focusOut");
      EffectManager.mx_internal::registerEffectTrigger("hideEffect", "hide");
      EffectManager.mx_internal::registerEffectTrigger("itemsChangeEffect", "itemsChange");
      EffectManager.mx_internal::registerEffectTrigger("mouseDownEffect", "mouseDown");
      EffectManager.mx_internal::registerEffectTrigger("mouseUpEffect", "mouseUp");
      EffectManager.mx_internal::registerEffectTrigger("moveEffect", "move");
      EffectManager.mx_internal::registerEffectTrigger("removedEffect", "removed");
      EffectManager.mx_internal::registerEffectTrigger("resizeEffect", "resize");
      EffectManager.mx_internal::registerEffectTrigger("resizeEndEffect", "resizeEnd");
      EffectManager.mx_internal::registerEffectTrigger("resizeStartEffect", "resizeStart");
      EffectManager.mx_internal::registerEffectTrigger("rollOutEffect", "rollOut");
      EffectManager.mx_internal::registerEffectTrigger("rollOverEffect", "rollOver");
      EffectManager.mx_internal::registerEffectTrigger("showEffect", "show");
      try {
      if (flash.net.getClassByAlias("flex.messaging.io.ArrayCollection") == null){
          flash.net.registerClassAlias("flex.messaging.io.ArrayCollection", mx.collections.ArrayCollection);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.io.ArrayCollection", mx.collections.ArrayCollection); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.io.ArrayList") == null){
          flash.net.registerClassAlias("flex.messaging.io.ArrayList", mx.collections.ArrayList);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.io.ArrayList", mx.collections.ArrayList); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.config.ConfigMap") == null){
          flash.net.registerClassAlias("flex.messaging.config.ConfigMap", mx.messaging.config.ConfigMap);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.config.ConfigMap", mx.messaging.config.ConfigMap); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.messages.AcknowledgeMessage") == null){
          flash.net.registerClassAlias("flex.messaging.messages.AcknowledgeMessage", mx.messaging.messages.AcknowledgeMessage);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.messages.AcknowledgeMessage", mx.messaging.messages.AcknowledgeMessage); }
      try {
      if (flash.net.getClassByAlias("DSK") == null){
          flash.net.registerClassAlias("DSK", mx.messaging.messages.AcknowledgeMessageExt);}
      } catch (e:Error) {
          flash.net.registerClassAlias("DSK", mx.messaging.messages.AcknowledgeMessageExt); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.messages.AsyncMessage") == null){
          flash.net.registerClassAlias("flex.messaging.messages.AsyncMessage", mx.messaging.messages.AsyncMessage);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.messages.AsyncMessage", mx.messaging.messages.AsyncMessage); }
      try {
      if (flash.net.getClassByAlias("DSA") == null){
          flash.net.registerClassAlias("DSA", mx.messaging.messages.AsyncMessageExt);}
      } catch (e:Error) {
          flash.net.registerClassAlias("DSA", mx.messaging.messages.AsyncMessageExt); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.messages.CommandMessage") == null){
          flash.net.registerClassAlias("flex.messaging.messages.CommandMessage", mx.messaging.messages.CommandMessage);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.messages.CommandMessage", mx.messaging.messages.CommandMessage); }
      try {
      if (flash.net.getClassByAlias("DSC") == null){
          flash.net.registerClassAlias("DSC", mx.messaging.messages.CommandMessageExt);}
      } catch (e:Error) {
          flash.net.registerClassAlias("DSC", mx.messaging.messages.CommandMessageExt); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.messages.ErrorMessage") == null){
          flash.net.registerClassAlias("flex.messaging.messages.ErrorMessage", mx.messaging.messages.ErrorMessage);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.messages.ErrorMessage", mx.messaging.messages.ErrorMessage); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.messages.MessagePerformanceInfo") == null){
          flash.net.registerClassAlias("flex.messaging.messages.MessagePerformanceInfo", mx.messaging.messages.MessagePerformanceInfo);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.messages.MessagePerformanceInfo", mx.messaging.messages.MessagePerformanceInfo); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.messages.RemotingMessage") == null){
          flash.net.registerClassAlias("flex.messaging.messages.RemotingMessage", mx.messaging.messages.RemotingMessage);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.messages.RemotingMessage", mx.messaging.messages.RemotingMessage); }
      try {
      if (flash.net.getClassByAlias("flex.messaging.io.ObjectProxy") == null){
          flash.net.registerClassAlias("flex.messaging.io.ObjectProxy", mx.utils.ObjectProxy);}
      } catch (e:Error) {
          flash.net.registerClassAlias("flex.messaging.io.ObjectProxy", mx.utils.ObjectProxy); }
      try {
      if (flash.net.getClassByAlias("org.granite.collections.BasicMap") == null){
          flash.net.registerClassAlias("org.granite.collections.BasicMap", org.granite.collections.BasicMap);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.collections.BasicMap", org.granite.collections.BasicMap); }
      try {
      if (flash.net.getClassByAlias("org.granite.collections.UIDList") == null){
          flash.net.registerClassAlias("org.granite.collections.UIDList", org.granite.collections.UIDList);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.collections.UIDList", org.granite.collections.UIDList); }
      try {
      if (flash.net.getClassByAlias("org.granite.collections.UIDSet") == null){
          flash.net.registerClassAlias("org.granite.collections.UIDSet", org.granite.collections.UIDSet);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.collections.UIDSet", org.granite.collections.UIDSet); }
      try {
      if (flash.net.getClassByAlias("java.math.BigDecimal") == null){
          flash.net.registerClassAlias("java.math.BigDecimal", org.granite.math.BigDecimal);}
      } catch (e:Error) {
          flash.net.registerClassAlias("java.math.BigDecimal", org.granite.math.BigDecimal); }
      try {
      if (flash.net.getClassByAlias("java.math.BigInteger") == null){
          flash.net.registerClassAlias("java.math.BigInteger", org.granite.math.BigInteger);}
      } catch (e:Error) {
          flash.net.registerClassAlias("java.math.BigInteger", org.granite.math.BigInteger); }
      try {
      if (flash.net.getClassByAlias("java.lang.Long") == null){
          flash.net.registerClassAlias("java.lang.Long", org.granite.math.Long);}
      } catch (e:Error) {
          flash.net.registerClassAlias("java.lang.Long", org.granite.math.Long); }
      try {
      if (flash.net.getClassByAlias("java.math.MathContext") == null){
          flash.net.registerClassAlias("java.math.MathContext", org.granite.math.MathContext);}
      } catch (e:Error) {
          flash.net.registerClassAlias("java.math.MathContext", org.granite.math.MathContext); }
      try {
      if (flash.net.getClassByAlias("java.math.RoundingMode") == null){
          flash.net.registerClassAlias("java.math.RoundingMode", org.granite.math.RoundingMode);}
      } catch (e:Error) {
          flash.net.registerClassAlias("java.math.RoundingMode", org.granite.math.RoundingMode); }
      try {
      if (flash.net.getClassByAlias("org.granite.messaging.persistence.ExternalizablePersistentBag") == null){
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentBag", org.granite.persistence.PersistentBag);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentBag", org.granite.persistence.PersistentBag); }
      try {
      if (flash.net.getClassByAlias("org.granite.messaging.persistence.ExternalizablePersistentList") == null){
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentList", org.granite.persistence.PersistentList);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentList", org.granite.persistence.PersistentList); }
      try {
      if (flash.net.getClassByAlias("org.granite.messaging.persistence.ExternalizablePersistentMap") == null){
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentMap", org.granite.persistence.PersistentMap);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentMap", org.granite.persistence.PersistentMap); }
      try {
      if (flash.net.getClassByAlias("org.granite.messaging.persistence.ExternalizablePersistentSet") == null){
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentSet", org.granite.persistence.PersistentSet);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.messaging.persistence.ExternalizablePersistentSet", org.granite.persistence.PersistentSet); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.TideMessage") == null){
          flash.net.registerClassAlias("org.granite.tide.TideMessage", org.granite.tide.TideMessage);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.TideMessage", org.granite.tide.TideMessage); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.invocation.ContextEvent") == null){
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextEvent", org.granite.tide.invocation.ContextEvent);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextEvent", org.granite.tide.invocation.ContextEvent); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.invocation.ContextEventListener") == null){
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextEventListener", org.granite.tide.invocation.ContextEventListener);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextEventListener", org.granite.tide.invocation.ContextEventListener); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.invocation.ContextResult") == null){
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextResult", org.granite.tide.invocation.ContextResult);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextResult", org.granite.tide.invocation.ContextResult); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.invocation.ContextUpdate") == null){
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextUpdate", org.granite.tide.invocation.ContextUpdate);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.invocation.ContextUpdate", org.granite.tide.invocation.ContextUpdate); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.invocation.InvocationCall") == null){
          flash.net.registerClassAlias("org.granite.tide.invocation.InvocationCall", org.granite.tide.invocation.InvocationCall);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.invocation.InvocationCall", org.granite.tide.invocation.InvocationCall); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.invocation.InvocationResult") == null){
          flash.net.registerClassAlias("org.granite.tide.invocation.InvocationResult", org.granite.tide.invocation.InvocationResult);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.invocation.InvocationResult", org.granite.tide.invocation.InvocationResult); }
      try {
      if (flash.net.getClassByAlias("org.granite.tide.validators.InvalidValue") == null){
          flash.net.registerClassAlias("org.granite.tide.validators.InvalidValue", org.granite.tide.validators.InvalidValue);}
      } catch (e:Error) {
          flash.net.registerClassAlias("org.granite.tide.validators.InvalidValue", org.granite.tide.validators.InvalidValue); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.entity.AbstractEntity") == null){
          flash.net.registerClassAlias("test.granite.ejb3.entity.AbstractEntity", test.granite.ejb3.entity.AbstractEntity);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.entity.AbstractEntity", test.granite.ejb3.entity.AbstractEntity); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.entity.Address") == null){
          flash.net.registerClassAlias("test.granite.ejb3.entity.Address", test.granite.ejb3.entity.Address);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.entity.Address", test.granite.ejb3.entity.Address); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.entity.Contact") == null){
          flash.net.registerClassAlias("test.granite.ejb3.entity.Contact", test.granite.ejb3.entity.Contact);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.entity.Contact", test.granite.ejb3.entity.Contact); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.entity.Country") == null){
          flash.net.registerClassAlias("test.granite.ejb3.entity.Country", test.granite.ejb3.entity.Country);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.entity.Country", test.granite.ejb3.entity.Country); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.entity.Person") == null){
          flash.net.registerClassAlias("test.granite.ejb3.entity.Person", test.granite.ejb3.entity.Person);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.entity.Person", test.granite.ejb3.entity.Person); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.entity.Person$Salutation") == null){
          flash.net.registerClassAlias("test.granite.ejb3.entity.Person$Salutation", test.granite.ejb3.entity.Person$Salutation);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.entity.Person$Salutation", test.granite.ejb3.entity.Person$Salutation); }
      try {
      if (flash.net.getClassByAlias("test.granite.ejb3.service.PersonService") == null){
          flash.net.registerClassAlias("test.granite.ejb3.service.PersonService", test.granite.ejb3.service.PersonService);}
      } catch (e:Error) {
          flash.net.registerClassAlias("test.granite.ejb3.service.PersonService", test.granite.ejb3.service.PersonService); }
      var styleNames:Array = ["fontAntiAliasType", "errorColor", "horizontalGridLineColor", "kerning", "backgroundDisabledColor", "iconColor", "modalTransparencyColor", "textRollOverColor", "textIndent", "verticalGridLineColor", "themeColor", "modalTransparency", "textDecoration", "headerColors", "fontThickness", "textAlign", "fontFamily", "textSelectedColor", "selectionDisabledColor", "labelWidth", "fontGridFitType", "letterSpacing", "rollOverColor", "fontStyle", "dropShadowColor", "fontSize", "selectionColor", "disabledColor", "dropdownBorderColor", "indicatorGap", "fontWeight", "disabledIconColor", "modalTransparencyBlur", "leading", "color", "alternatingItemColors", "fontSharpness", "barColor", "modalTransparencyDuration", "footerColors"];

      import mx.styles.StyleManager;

      for (var i:int = 0; i < styleNames.length; i++)
      {
         StyleManager.registerInheritingStyle(styleNames[i]);
      }
   }
}  // FlexInit
}  // package
