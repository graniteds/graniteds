package org.granite.util {

  import flash.utils.Dictionary;
  import flash.utils.describeType;
  import flash.utils.getDefinitionByName;
  import flash.utils.getQualifiedClassName;
  import flash.net.registerClassAlias;

public  class GraniteClassRegistry {

    static var destinationClasses: Dictionary = new Dictionary();

    public static function registerClasses(destination:String, classes:Array):void {
          var aliasMap: Dictionary = new Dictionary();

          for each(var clazz: Class in classes)
          {
              aliasMap[getAlias(clazz)] = clazz;
          }

          destinationClasses[destination] = aliasMap;
    }

    public static function unregisterClasses(destination:String):void {
         delete destinationClasses[destination];
    }

    public static function useClasses(destination:String): void {
         var aliasMap: Dictionary = destinationClasses[destination];
         if(aliasMap)
         {
             for(var alias: String in aliasMap)
             {
                 registerClassAlias(alias, aliasMap[alias]);
             }
         }
    }

    public static function getAlias(clazz: Object): String {

        var classInfo:XML = describeType(clazz);
        var remoteClass:XMLList = classInfo.factory.metadata.(@name = "RemoteClass");
        var remoteClassTag:XML;
        var remoteClassAlias:String;
        for each (remoteClassTag in remoteClass) {
            if (remoteClassTag.elements("arg").length() == 1) {
                remoteClassAlias = remoteClassTag.arg.(@key = "alias").@value.toString();
            }
        }

        // If a remote class alias was found use it as a registration name, otherwise use its full qualified class name.
        var registrationName:String;
        if (remoteClassAlias) {
            registrationName = remoteClassAlias;
        }
        else {
           registrationName = getQualifiedClassName(clazz);
        }

        return registrationName;
    }
}
}