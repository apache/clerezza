function testString(f_sString)
{
	if (f_sString.length > 0)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testStringEq(f_sString, f_iLen)
{
	if (f_sString.length == f_iLen)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testStringGt(f_sString, f_iLen)
{
	if (f_sString.length > f_iLen)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testStringLt(f_sString, f_iLen)
{
	if (f_sString.length < f_iLen)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testInt(f_iInteger)
{
	var regInt = /^[0-9]+$/;
	
	if (regInt.test(f_iInteger) == true)
	{
		return true;
	}	
	else
	{
		return false;
	}
}

function testFloat(f_flFloat)
{
	var regFloat = /^[0-9]+(\.[0-9]+)*$/;
	
	if (regFloat.test(f_flFloat) == true)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testIntGtZero(f_iInteger)
{
	var regInt = /^[0-9]+$/;
	
	if (regInt.test(f_iInteger) == true && f_iInteger > 0)
	{
		return true;
	}	
	else
	{
		return false;
	}
}

function testMinusOne(f_iInteger)
{
	if (f_iInteger != -1)
	{
		return true;
	}	
	else
	{
		return false;
	}
}

function testTime(f_sTime)
{
	var regTime = /^([0-2]?[0-9]):[0-5][0-9](:[0-5][0-9])?$/;
	var iIndex;
	var iValue;
	var sValue = f_sTime;
	
	if (regTime.test(sValue) == true)
	{
		iIndex = sValue.indexOf(":");
		iValue = sValue.substr(0,iIndex);
		
		if (iValue <= 23 && iValue >=0)
		{
			return true;
		}
		else
		{
			return false;
		}	
	}
	else
	{
		return false;
	}
}

function testDate(f_sDate)
{
	var regDate = /^([0-3]?[0-9])\.([0-1]?[0-9])\.[1-9][0-9]{3}$/;
	var iIndex, iIndex2;
	var iMonth,iDay,iYear;
	var arrMonth = new Array(31,28,31,30,31,30,31,31,30,31,30,31);
	var sValue = f_sDate;
	
	if (regDate.test(sValue) == true)
	{
		iIndex = sValue.indexOf(".");
		iIndex2 = sValue.indexOf(".",iIndex+1);
		
		iDay = sValue.substr(0,iIndex);
		iMonth = sValue.substring(iIndex+1,iIndex2);
		iYear = sValue.substr(iIndex2+1,sValue.length);		

		if (iMonth < 1 || iMonth > 12)
		{
			return false;		
		}
		
		if (iMonth == 2 && ( ( (iYear % 4) == 0 && (iYear % 100) != 0 ) || (iYear % 400) == 0 ) ) 
		{
			if (iDay < 1 || iDay > 29)
			{
				return false;
			}
		}
		else
		{
			if (iDay < 1 || iDay > arrMonth[iMonth-1])
			{
				return false;
			}
		}		

		return true;

	}
	else
	{
		return false;
	}
}

/* For dates that are formated different to normal dates (i.e. MM.DD.YYYY) */
function testDate2(f_sDate)
{
	var regDate = /^([0-1]?[0-9])\.([0-3]?[0-9])\.[1-9][0-9]{3}$/;
	var iIndex, iIndex2;
	var iMonth,iDay,iYear;
	var arrMonth = new Array(31,28,31,30,31,30,31,31,30,31,30,31);
	var sValue = f_sDate;
	
	if (regDate.test(sValue) == true)
	{
		iIndex = sValue.indexOf(".");
		iIndex2 = sValue.indexOf(".",iIndex+1);
		
		iMonth = sValue.substr(0,iIndex);
		iDay = sValue.substring(iIndex+1,iIndex2);
		iYear = sValue.substr(iIndex2+1,sValue.length);		

		if (iMonth < 1 || iMonth > 12)
		{
			return false;		
		}
		
		if (iMonth == 2 && ( ( (iYear % 4) == 0 && (iYear % 100) != 0 ) || (iYear % 400) == 0 ) ) 
		{
			if (iDay < 1 || iDay > 29)
			{
				return false;
			}
		}
		else
		{
			if (iDay < 1 || iDay > arrMonth[iMonth-1])
			{
				return false;
			}
		}		

		return true;

	}
	else
	{
		return false;
	}
}

function compareDate(f_sDate1,f_sDate2)
{
	var sValue1 = f_sDate1;
	var sValue2 = f_sDate2;
	
	iIndex = sValue1.indexOf(".");
	iIndex2 = sValue1.indexOf(".",iIndex+1);
	
	iDay1 = sValue1.substr(0,iIndex);
	iDay1 = (iDay1.length == 2) ? iDay1 : iDay1 = "0" + iDay1 ; 
	iMonth1 = sValue1.substring(iIndex+1,iIndex2);
	iMonth1 = (iMonth1.length == 2) ? iMonth1 : iMonth1 = "0" + iMonth1 ; 
	iYear1 = sValue1.substr(iIndex2+1,sValue1.length);	

	
	iIndex = sValue2.indexOf(".");
	iIndex2 = sValue2.indexOf(".",iIndex+1);
	
	iDay2 = sValue2.substr(0,iIndex);
	iDay2 = (iDay2.length == 2) ? iDay2 : iDay2 = "0" + iDay2 ; 
	iMonth2 = sValue2.substring(iIndex+1,iIndex2);
	iMonth2 = (iMonth2.length == 2) ? iMonth2: iMonth2 = "0" + iMonth2 ; 
	iYear2 = sValue2.substr(iIndex2+1,sValue2.length);		
	
	iDate1 = parseInt(iYear1 + iMonth1 + iDay1);
	iDate2 = parseInt(iYear2 + iMonth2 + iDay2);
	
	if(iDate1 <= iDate2)
	{
		return true
	}
	else
	{
		return false;
	}
}

function testEmail(f_sEmail)
{
	var regEmail = /^[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*\.([a-zA-Z]{2,4})$/;
	
	if (regEmail.test(f_sEmail) == true)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testChPlz(f_iPLZ)
{
	var regPLZ = /^[1-9][0-9]{3}$/;
	
	if (regPLZ.test(f_iPLZ) == true)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testCHMoney(f_flMoney)
{
	var regMoney = /(^[1-9][0-9]*|^0)(\.[0-9](0|5))?$/;
	
	if (regMoney.test(f_flMoney) == true)
	{
		return true;
	}
	else
	{
		return false;
	}
}

function testEmpty(f_sString)
{
	var regWhiteSpace = /[^ \f\n\r\t]/;
	
	if (regWhiteSpace.test(f_sString))
	{
		return false;
	}
	else
	{
		return true;
	}
}

function testReg(f_sString,f_regString)
{

	regString = new RegExp(f_regString,"gi");
	if (regString.test(f_sString) == true)
	{
		return true;
	}
	else
	{
		return false;
	}
}
