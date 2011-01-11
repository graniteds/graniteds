package org.jboss.seam.example.booking.control {

    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
	import org.granite.tide.seam.In;
	import org.granite.tide.seam.security.Identity;

    import org.jboss.seam.example.booking.User;

    [Bindable]
    [Name("userCtl")]
    public class UserCtl {
    
        [In]
        public var identity:Identity; 

        [In] [Out(remote="true")]
        public var user:User; 
        
        [In]
        public var register:Object; 
        
        [In]
        public var changePassword:Object; 

		///////////////////////////////////////////////////////////////////////
		// Login
		
        [Observer("login")]
        public function login(username:String, password:String):void {
			identity.username = username;
            identity.password = password;
            identity.login(loginResult);
        }
        
        private function loginResult(event:TideResultEvent):void {
            event.context.raiseEvent("showSearchView", false);
        }

		///////////////////////////////////////////////////////////////////////
		// Logout

        [Observer("logout")]
        public function logout(event:TideContextEvent):void {
            identity.logout();
        }

		///////////////////////////////////////////////////////////////////////
		// Register

        [Observer("register")]
        public function registerUser(name:String, username:String, password:String, verify:String):void	{
			user = new User(name, username, password);
            register.verify = verify;

			In(register.registered)
            register.register(registerResult);
        }

        private function registerResult(event:TideResultEvent):void {
			if (register.registered)
				event.context.raiseEvent("showLoginView", false);
        }

		///////////////////////////////////////////////////////////////////////
		// Change Password

        [Observer("changePassword")]
        public function changeUserPassword(password:String, verify:String):void {
            user.password = password;
            changePassword.verify = verify;

			In(changePassword.changed)
            changePassword.changePassword(changePasswordResult);
        }

        private function changePasswordResult(event:TideResultEvent):void {
			if (changePassword.changed)
				event.context.raiseEvent("showSearchView", false);
        }
	}
}
