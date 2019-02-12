package org.apache.clerezza.ontologies;

import org.apache.clerezza.api.IRI;

public class XSD {
	// Classes

	/**
	 * comment: NCName represents XML "non-colonized" Names. The ·value space· of NCName is the set of all strings which ·match· the NCName production of [Namespaces in XML]. The ·lexical space· of NCName is the set of all strings which ·match· the NCName production of [Namespaces in XML]. The ·base type· of NCName is Name.

	 */
	public static final IRI NCName = new IRI("http://www.w3.org/2001/XMLSchema#NCName");

	/**
	 * comment: NMTOKEN represents the NMTOKEN attribute type from [XML 1.0 (Second Edition)]. The ·value space· of NMTOKEN is the set of tokens that ·match· the Nmtoken production in [XML 1.0 (Second Edition)]. The ·lexical space· of NMTOKEN is the set of strings that ·match· the Nmtoken production in [XML 1.0 (Second Edition)]. The ·base type· of NMTOKEN is token.

	 */
	public static final IRI NMTOKEN = new IRI("http://www.w3.org/2001/XMLSchema#NMTOKEN");

	/**
	 * comment: Name represents XML Names. The ·value space· of Name is the set of all strings which ·match· the Name production of [XML 1.0 (Second Edition)]. The ·lexical space· of Name is the set of all strings which ·match· the Name production of [XML 1.0 (Second Edition)]. The ·base type· of Name is token.

	 */
	public static final IRI Name = new IRI("http://www.w3.org/2001/XMLSchema#Name");

	/**
	 * comment: anyURI represents a Uniform Resource Identifier Reference (URI). An anyURI value can be absolute or relative, and may have an optional fragment identifier (i.e., it may be a URI Reference). This type should be used to specify the intention that the value fulfills the role of a URI as defined by [RFC 2396], as amended by [RFC 2732].

	 */
	public static final IRI anyURI = new IRI("http://www.w3.org/2001/XMLSchema#anyURI");

	/**
	 * comment:  base64Binary represents Base64-encoded arbitrary binary data. The ·value space· of base64Binary is the set of finite-length sequences of binary octets. For base64Binary data the entire binary stream is encoded using the Base64 Content-Transfer-Encoding defined in Section 6.8 of [RFC 2045].

	 */
	public static final IRI base64Binary = new IRI("http://www.w3.org/2001/XMLSchema#base64Binary");

	/**
	 * comment: boolean has the ·value space· required to support the mathematical concept of binary-valued logic: {true, false}.

	 */
	public static final IRI boolean_ = new IRI("http://www.w3.org/2001/XMLSchema#boolean");

	/**
	 * comment:  byte is ·derived· from short by setting the value of ·maxInclusive· to be 127 and ·minInclusive· to be -128. The ·base type· of byte is short.

	 */
	public static final IRI byte_ = new IRI("http://www.w3.org/2001/XMLSchema#byte");

	/**
	 * comment: date represents a calendar date. The ·value space· of date is the set of Gregorian calendar dates as defined in § 5.2.1 of [ISO 8601]. Specifically, it is a set of one-day long, non-periodic instances e.g. lexical 1999-10-26 to represent the calendar date 1999-10-26, independent of how many hours this day has.

	 */
	public static final IRI date = new IRI("http://www.w3.org/2001/XMLSchema#date");

	/**
	 * comment: dateTime represents a specific instant of time. The ·value space· of dateTime is the space of Combinations of date and time of day values as defined in § 5.4 of [ISO 8601].

	 */
	public static final IRI dateTime = new IRI("http://www.w3.org/2001/XMLSchema#dateTime");

	/**
	 * comment: decimal represents arbitrary precision decimal numbers. The ·value space· of decimal is the set of the values i × 10^-n, where i and n are integers such that n >= 0. The ·order-relation· on decimal is: x < y iff y - x is positive.

[Definition:]   The ·value space· of types derived from decimal with a value for ·totalDigits· of p is the set of values i × 10^-n, where n and i are integers such that p >= n >= 0 and the number of significant decimal digits in i is less than or equal to p.

[Definition:]   The ·value space· of types derived from decimal with a value for ·fractionDigits· of s is the set of values i × 10^-n, where i and n are integers such that 0 <= n <= s.

	 */
	public static final IRI decimal = new IRI("http://www.w3.org/2001/XMLSchema#decimal");

	/**
	 * comment: The double datatype corresponds to IEEE double-precision 64-bit floating point type [IEEE 754-1985]. The basic ·value space· of double consists of the values m × 2^e, where m is an integer whose absolute value is less than 2^53, and e is an integer between -1075 and 970, inclusive. In addition to the basic ·value space· described above, the ·value space· of double also contains the following special values: positive and negative zero, positive and negative infinity and not-a-number. The ·order-relation· on double is: x < y iff y - x is positive. Positive zero is greater than negative zero. Not-a-number equals itself and is greater than all double values including positive infinity.

	 */
	public static final IRI double_ = new IRI("http://www.w3.org/2001/XMLSchema#double");

	/**
	 * comment: float corresponds to the IEEE single-precision 32-bit floating point type [IEEE 754-1985]. The basic ·value space· of float consists of the values m × 2^e, where m is an integer whose absolute value is less than 2^24, and e is an integer between -149 and 104, inclusive. In addition to the basic ·value space· described above, the ·value space· of float also contains the following special values: positive and negative zero, positive and negative infinity and not-a-number. The ·order-relation· on float is: x < y iff y - x is positive. Positive zero is greater than negative zero. Not-a-number equals itself and is greater than all float values including positive infinity.

	 */
	public static final IRI float_ = new IRI("http://www.w3.org/2001/XMLSchema#float");

	/**
	 * comment: gDay is a gregorian day that recurs, specifically a day of the month such as the 5th of the month. Arbitrary recurring days are not supported by this datatype. The ·value space· of gDay is the space of a set of calendar dates as defined in § 3 of [ISO 8601]. Specifically, it is a set of one-day long, monthly periodic instances.

	 */
	public static final IRI gDay = new IRI("http://www.w3.org/2001/XMLSchema#gDay");

	/**
	 * comment:  gMonth is a gregorian month that recurs every year. The ·value space· of gMonth is the space of a set of calendar months as defined in § 3 of [ISO 8601]. Specifically, it is a set of one-month long, yearly periodic instances.

	 */
	public static final IRI gMonth = new IRI("http://www.w3.org/2001/XMLSchema#gMonth");

	/**
	 * comment: gMonthDay is a gregorian date that recurs, specifically a day of the year such as the third of May. Arbitrary recurring dates are not supported by this datatype. The ·value space· of gMonthDay is the set of calendar dates, as defined in § 3 of [ISO 8601]. Specifically, it is a set of one-day long, annually periodic instances.

	 */
	public static final IRI gMonthDay = new IRI("http://www.w3.org/2001/XMLSchema#gMonthDay");

	/**
	 * comment: gYearMonth represents a specific gregorian month in a specific gregorian year. The ·value space· of gYearMonth is the set of Gregorian calendar months as defined in § 5.2.1 of [ISO 8601]. Specifically, it is a set of one-month long, non-periodic instances e.g. 1999-10 to represent the whole month of 1999-10, independent of how many days this month has.

	 */
	public static final IRI gYearMonth = new IRI("http://www.w3.org/2001/XMLSchema#gYearMonth");

	/**
	 * comment: hexBinary represents arbitrary hex-encoded binary data. The ·value space· of hexBinary is the set of finite-length sequences of binary octets.

	 */
	public static final IRI hexBinary = new IRI("http://www.w3.org/2001/XMLSchema#hexBinary");

	/**
	 * comment: int is ·derived· from long by setting the value of ·maxInclusive· to be 2147483647 and ·minInclusive· to be -2147483648. The ·base type· of int is long.

	 */
	public static final IRI int_ = new IRI("http://www.w3.org/2001/XMLSchema#int");

	/**
	 * comment: integer is ·derived· from decimal by fixing the value of ·fractionDigits· to be 0. This results in the standard mathematical concept of the integer numbers. The ·value space· of integer is the infinite set {...,-2,-1,0,1,2,...}. The ·base type· of integer is decimal.

	 */
	public static final IRI integer = new IRI("http://www.w3.org/2001/XMLSchema#integer");

	/**
	 * comment: anguage represents natural language identifiers as defined by [RFC 1766]. The ·value space· of language is the set of all strings that are valid language identifiers as defined in the language identification section of [XML 1.0 (Second Edition)]. The ·lexical space· of language is the set of all strings that are valid language identifiers as defined in the language identification section of [XML 1.0 (Second Edition)]. The ·base type· of language is token.

	 */
	public static final IRI language = new IRI("http://www.w3.org/2001/XMLSchema#language");

	/**
	 * comment: long is ·derived· from integer by setting the value of ·maxInclusive· to be 9223372036854775807 and ·minInclusive· to be -9223372036854775808. The ·base type· of long is integer.

	 */
	public static final IRI long_ = new IRI("http://www.w3.org/2001/XMLSchema#long");

	/**
	 * comment: negativeInteger is ·derived· from nonPositiveInteger by setting the value of ·maxInclusive· to be -1. This results in the standard mathematical concept of the negative integers. The ·value space· of negativeInteger is the infinite set {...,-2,-1}. The ·base type· of negativeInteger is nonPositiveInteger.

	 */
	public static final IRI negativeInteger = new IRI("http://www.w3.org/2001/XMLSchema#negativeInteger");

	/**
	 * comment: nonNegativeInteger is ·derived· from integer by setting the value of ·minInclusive· to be 0. This results in the standard mathematical concept of the non-negative integers. The ·value space· of nonNegativeInteger is the infinite set {0,1,2,...}. The ·base type· of nonNegativeInteger is integer.

	 */
	public static final IRI nonNegativeInteger = new IRI("http://www.w3.org/2001/XMLSchema#nonNegativeInteger");

	/**
	 * comment: nonPositiveInteger is ·derived· from integer by setting the value of ·maxInclusive· to be 0. This results in the standard mathematical concept of the non-positive integers. The ·value space· of nonPositiveInteger is the infinite set {...,-2,-1,0}. The ·base type· of nonPositiveInteger is integer.

	 */
	public static final IRI nonPositiveInteger = new IRI("http://www.w3.org/2001/XMLSchema#nonPositiveInteger");

	/**
	 * comment: normalizedString represents white space normalized strings. The ·value space· of normalizedString is the set of strings that do not contain the carriage return (#xD), line feed (#xA) nor tab (#x9) characters. The ·lexical space· of normalizedString is the set of strings that do not contain the carriage return (#xD) nor tab (#x9) characters. The ·base type· of normalizedString is string.  

	 */
	public static final IRI normalizedString = new IRI("http://www.w3.org/2001/XMLSchema#normalizedString");

	/**
	 * comment: positiveInteger is ·derived· from nonNegativeInteger by setting the value of ·minInclusive· to be 1. This results in the standard mathematical concept of the positive integer numbers. The ·value space· of positiveInteger is the infinite set {1,2,...}. The ·base type· of positiveInteger is nonNegativeInteger.

	 */
	public static final IRI positiveInteger = new IRI("http://www.w3.org/2001/XMLSchema#positiveInteger");

	/**
	 * comment: short is ·derived· from int by setting the value of ·maxInclusive· to be 32767 and ·minInclusive· to be -32768. The ·base type· of short is int.

	 */
	public static final IRI short_ = new IRI("http://www.w3.org/2001/XMLSchema#short");

	/**
	 * comment: The string datatype represents character strings in XML. The ·value space· of string is the set of finite-length sequences of characters (as defined in [XML 1.0 (Second Edition)]) that ·match· the Char production from [XML 1.0 (Second Edition)]. A character is an atomic unit of communication; it is not further specified except to note that every character has a corresponding Universal Character Set code point, which is an integer.

	 */
	public static final IRI string = new IRI("http://www.w3.org/2001/XMLSchema#string");

	/**
	 * comment:   time represents an instant of time that recurs every day. The ·value space· of time is the space of time of day values as defined in § 5.3 of [ISO 8601]. Specifically, it is a set of zero-duration daily time instances.

	 */
	public static final IRI time = new IRI("http://www.w3.org/2001/XMLSchema#time");

	/**
	 * comment:  token represents tokenized strings. The ·value space· of token is the set of strings that do not contain the line feed (#xA) nor tab (#x9) characters, that have no leading or trailing spaces (#x20) and that have no internal sequences of two or more spaces. The ·lexical space· of token is the set of strings that do not contain the line feed (#xA) nor tab (#x9) characters, that have no leading or trailing spaces (#x20) and that have no internal sequences of two or more spaces. The ·base type· of token is normalizedString.

	 */
	public static final IRI token = new IRI("http://www.w3.org/2001/XMLSchema#token");

	/**
	 * comment: unsignedByte is ·derived· from unsignedShort by setting the value of ·maxInclusive· to be 255. The ·base type· of unsignedByte is unsignedShort.

	 */
	public static final IRI unsignedByte = new IRI("http://www.w3.org/2001/XMLSchema#unsignedByte");

	/**
	 * comment:  unsignedInt is ·derived· from unsignedLong by setting the value of ·maxInclusive· to be 4294967295. The ·base type· of unsignedInt is unsignedLong.

	 */
	public static final IRI unsignedInt = new IRI("http://www.w3.org/2001/XMLSchema#unsignedInt");

	/**
	 * comment: unsignedLong is ·derived· from nonNegativeInteger by setting the value of ·maxInclusive· to be 18446744073709551615. The ·base type· of unsignedLong is nonNegativeInteger.

	 */
	public static final IRI unsignedLong = new IRI("http://www.w3.org/2001/XMLSchema#unsignedLong");

	/**
	 * comment: unsignedShort is ·derived· from unsignedInt by setting the value of ·maxInclusive· to be 65535. The ·base type· of unsignedShort is unsignedInt.

	 */
	public static final IRI unsignedShort = new IRI("http://www.w3.org/2001/XMLSchema#unsignedShort");

	// Properties

	/**
	 * 
	 */
	public static final IRI XMLSchema = new IRI("http://www.w3.org/2001/XMLSchema");
}
