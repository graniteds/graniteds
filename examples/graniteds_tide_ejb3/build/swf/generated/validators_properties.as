package 
{

import mx.resources.ResourceBundle;

[ExcludeClass]

public class en_US$validators_properties extends ResourceBundle
{

    public function en_US$validators_properties()
    {
		 super("en_US", "validators");
    }

    override protected function getContent():Object
    {
        var content:Object =
        {
            "PAttributeMissing": "The property attribute must be specified when the source attribute is specified.",
            "wrongLengthErrorDV": "Type the date in the format.",
            "maxLength": "NaN",
            "invalidDomainErrorZCV": "The domain parameter is invalid. It must be either 'US Only', 'Canada Only', or 'US or Canada'.",
            "creditCardValidatorAllowedFormatChars": " -",
            "wrongFormatError": "The Social Security number must be 9 digits or in the form NNN-NN-NNNN.",
            "invalidNumberError": "The credit card number is invalid.",
            "CNSAttribute": "The cardNumberSource attribute, '{0}', can not be of type String.",
            "invalidCharErrorCCV": "Invalid characters in your credit card number. (Enter numbers only.)",
            "thousandsSeparator": ",",
            "minDigitsPNV": "10",
            "wrongLengthErrorPNV": "Your telephone number must contain at least {0} digits.",
            "invalidPeriodsInDomainError": "The domain in your e-mail address has consecutive periods.",
            "precisionError": "The amount entered has too many digits beyond the decimal point.",
            "wrongUSFormatError": "The ZIP+4 code must be formatted '12345-6789'.",
            "separationError": "The thousands separator must be followed by three digits.",
            "DSAttribute": "The daySource attribute, '{0}', can not be of type String.",
            "zipCodeValidatorDomain": "US Only",
            "exceedsMaxErrorCV": "The amount entered is too large.",
            "allowNegative": "true",
            "decimalPointCountError": "The decimal separator can occur only once.",
            "requiredFieldError": "This field is required.",
            "missingPeriodInDomainError": "The domain in your e-mail address is missing a period.",
            "invalidCharError": "The input contains invalid characters.",
            "SAttribute": "The source attribute, '{0}', can not be of type String.",
            "wrongCAFormatError": "The Canadian postal code must be formatted 'A1B 2C3'.",
            "wrongLengthErrorCCV": "Your credit card number contains the wrong number of digits.",
            "tooShortError": "This string is shorter than the minimum allowed length. This must be at least {0} characters long.",
            "decimalSeparator": ".",
            "zeroStartError": "Invalid Social Security number; the number cannot start with 000.",
            "invalidFormatChars": "The allowedFormatChars parameter is invalid. It cannot contain any digits.",
            "validateAsString": "true",
            "invalidCharErrorZCV": "The ZIP code contains invalid characters.",
            "exceedsMaxErrorNV": "The number entered is too large.",
            "missingCardNumber": "The value being validated doesn't contain a cardNumber property.",
            "CTSAttribute": "The cardTypeSource attribute, '{0}', can not be of type String.",
            "numberValidatorPrecision": "-1",
            "YSAttribute": "The yearSource attribute, '{0}', can not be of type String.",
            "negativeError": "The amount may not be negative.",
            "fieldNotFound": "'{0}' field not found.",
            "noNumError": "No credit card number is specified.",
            "SAttributeMissing": "The source attribute must be specified when the property attribute is specified.",
            "noTypeError": "No credit card type is specified or the type is not valid.",
            "tooManyAtSignsError": "Your e-mail address contains too many @ characters.",
            "wrongLengthErrorZCV": "The ZIP code must be 5 digits or 5+4 digits.",
            "socialSecurityValidatorAllowedFormatChars": " -",
            "wrongYearError": "Enter a year between 0 and 9999.",
            "minLength": "NaN",
            "missingCardType": "The value being validated doesn't contain a cardType property.",
            "noExpressionError": "The expression is missing.",
            "maxValue": "NaN",
            "invalidDomainErrorEV": "The domain in your e-mail address is incorrectly formatted.",
            "numberValidatorDomain": "real",
            "minValue": "NaN",
            "missingUsernameError": "The username in your e-mail address is missing.",
            "invalidCharErrorEV": "Your e-mail address contains invalid characters.",
            "MSAttribute": "The monthSource attribute, '{0}', can not be of type String.",
            "phoneNumberValidatorAllowedFormatChars": "-()+ .",
            "noMatchError": "The field is invalid.",
            "wrongMonthError": "Enter a month between 1 and 12.",
            "invalidIPDomainError": "The IP domain in your e-mail address is incorrectly formatted.",
            "dateValidatorAllowedFormatChars": "/- \\.",
            "integerError": "The number must be an integer.",
            "currencyValidatorPrecision": "2",
            "invalidFormatCharsZCV": "The allowedFormatChars parameter is invalid. Alphanumeric characters are not allowed (a-z A-Z 0-9).",
            "formatError": "Configuration error: Incorrect formatting string.",
            "wrongDayError": "Enter a valid day for the month.",
            "lowerThanMinError": "The amount entered is too small.",
            "zipCodeValidatorAllowedFormatChars": " -",
            "invalidCharErrorPNV": "Your telephone number contains invalid characters.",
            "invalidCharErrorDV": "The date contains invalid characters.",
            "missingAtSignError": "An at sign (@) is missing in your e-mail address.",
            "invalidFormatCharsError": "One of the formatting parameters is invalid.",
            "wrongTypeError": "Incorrect card type is specified.",
            "tooLongError": "This string is longer than the maximum allowed length. This must be less than {0} characters long.",
            "currencySymbolError": "The currency symbol occurs in an invalid location.",
            "invalidCharErrorSSV": "You entered invalid characters in your Social Security number."
        };
        return content;
    }
}



}
