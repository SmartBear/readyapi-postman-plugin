<wsdl:definitions xmlns:s="http://www.w3.org/2001/XMLSchema"
                  xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/"
                  xmlns:http="http://schemas.xmlsoap.org/wsdl/http/"
                  xmlns:tns="http://localhost/test_service/Service1.asmx"
                  xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                  targetNamespace="http://localhost/test_service/Service1.asmx" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
    <wsdl:types>
        <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tns="http://localhost/test_service/Service1.asmx"
                   targetNamespace="http://localhost/test_service/Service1.asmx">
            <xs:element name="DateResponse">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element minOccurs="1" maxOccurs="1" name="DateResult" type="xs:dateTime"/>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
        </xs:schema>
    </wsdl:types>
    <wsdl:message name="DateSoapIn">
        <wsdl:part name="parameters" element="tns:Date"/>
    </wsdl:message>
    <wsdl:message name="DateSoapOut">
        <wsdl:part name="parameters" element="tns:DateResponse"/>
    </wsdl:message>
    <wsdl:message name="RanSoapIn">
        <wsdl:part name="parameters" element="tns:Ran"/>
    </wsdl:message>
    <wsdl:message name="RanSoapOut">
        <wsdl:part name="parameters" element="tns:RanResponse"/>
    </wsdl:message>
    <wsdl:message name="Ran_XMLSoapIn">
        <wsdl:part name="parameters" element="tns:Ran_XML"/>
    </wsdl:message>
    <wsdl:message name="Ran_XMLSoapOut">
        <wsdl:part name="parameters" element="tns:Ran_XMLResponse"/>
    </wsdl:message>
    <wsdl:message name="Date_XMLSoapIn">
        <wsdl:part name="parameters" element="tns:Date_XML"/>
    </wsdl:message>
    <wsdl:message name="Date_XMLSoapOut">
        <wsdl:part name="parameters" element="tns:Date_XMLResponse"/>
    </wsdl:message>
    <wsdl:message name="InputDateSoapIn">
        <wsdl:part name="parameters" element="tns:InputDate"/>
    </wsdl:message>
    <wsdl:message name="InputDateSoapOut">
        <wsdl:part name="parameters" element="tns:InputDateResponse"/>
    </wsdl:message>
    <wsdl:message name="DateHttpGetIn"/>
    <wsdl:message name="DateHttpGetOut">
        <wsdl:part name="Body" element="tns:dateTime"/>
    </wsdl:message>
    <wsdl:message name="RanHttpGetIn">
        <wsdl:part name="x" type="s:string"/>
    </wsdl:message>
    <wsdl:message name="RanHttpGetOut">
        <wsdl:part name="Body" element="tns:int"/>
    </wsdl:message>
    <wsdl:message name="Ran_XMLHttpGetIn">
        <wsdl:part name="x" type="s:string"/>
    </wsdl:message>
    <wsdl:message name="Ran_XMLHttpGetOut">
        <wsdl:part name="Body"/>
    </wsdl:message>
    <wsdl:message name="Date_XMLHttpGetIn"/>
    <wsdl:message name="Date_XMLHttpGetOut">
        <wsdl:part name="Body"/>
    </wsdl:message>
    <wsdl:message name="InputDateHttpGetIn">
        <wsdl:part name="MyInputDate" type="s:string"/>
    </wsdl:message>
    <wsdl:message name="InputDateHttpGetOut">
        <wsdl:part name="Body" element="tns:dateTime"/>
    </wsdl:message>
    <wsdl:message name="DateHttpPostIn"/>
    <wsdl:message name="DateHttpPostOut">
        <wsdl:part name="Body" element="tns:dateTime"/>
    </wsdl:message>
    <wsdl:message name="RanHttpPostIn">
        <wsdl:part name="x" type="s:string"/>
    </wsdl:message>
    <wsdl:message name="RanHttpPostOut">
        <wsdl:part name="Body" element="tns:int"/>
    </wsdl:message>
    <wsdl:message name="Ran_XMLHttpPostIn">
        <wsdl:part name="x" type="s:string"/>
    </wsdl:message>
    <wsdl:message name="Ran_XMLHttpPostOut">
        <wsdl:part name="Body"/>
    </wsdl:message>
    <wsdl:message name="Date_XMLHttpPostIn"/>
    <wsdl:message name="Date_XMLHttpPostOut">
        <wsdl:part name="Body"/>
    </wsdl:message>
    <wsdl:portType name="Test_serviceSoap">
        <wsdl:operation name="Date">
            <wsdl:documentation xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">The Date API</wsdl:documentation>
            <wsdl:input message="tns:DateSoapIn"/>
            <wsdl:output message="tns:DateSoapOut"/>
        </wsdl:operation>
        <wsdl:operation name="Ran">
            <wsdl:documentation xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">The Random API
            </wsdl:documentation>
            <wsdl:input message="tns:RanSoapIn"/>
            <wsdl:output message="tns:RanSoapOut"/>
        </wsdl:operation>
    </wsdl:portType>
    <wsdl:binding name="Test_serviceSoap" type="tns:Test_serviceSoap">
        <soap:binding transport="http://schemas.xmlsoap.org/soap/http"/>
        <wsdl:operation name="Date">
            <soap:operation soapAction="http://localhost/test_service/Service1.asmx/Date" style="document"/>
            <wsdl:input>
                <soap:body use="literal"/>
            </wsdl:input>
            <wsdl:output>
                <soap:body use="literal"/>
            </wsdl:output>
        </wsdl:operation>
        <wsdl:operation name="Ran">
            <soap:operation soapAction="http://localhost/test_service/Service1.asmx/Ran" style="document"/>
            <wsdl:input>
                <soap:body use="literal"/>
            </wsdl:input>
            <wsdl:output>
                <soap:body use="literal"/>
            </wsdl:output>
        </wsdl:operation>
    </wsdl:binding>
    <wsdl:binding name="Test_serviceSoap12" type="tns:Test_serviceSoap">
        <soap12:binding transport="http://schemas.xmlsoap.org/soap/http"/>
        <wsdl:operation name="Date">
            <soap12:operation soapAction="http://localhost/test_service/Service1.asmx/Date" style="document"/>
            <wsdl:input>
                <soap12:body use="literal"/>
            </wsdl:input>
            <wsdl:output>
                <soap12:body use="literal"/>
            </wsdl:output>
        </wsdl:operation>
        <wsdl:operation name="Ran">
            <soap12:operation soapAction="http://localhost/test_service/Service1.asmx/Ran" style="document"/>
            <wsdl:input>
                <soap12:body use="literal"/>
            </wsdl:input>
            <wsdl:output>
                <soap12:body use="literal"/>
            </wsdl:output>
        </wsdl:operation>
    </wsdl:binding>
    <wsdl:service name="Test_service">
        <wsdl:documentation xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">Test Service</wsdl:documentation>
        <wsdl:port name="Test_serviceSoap" binding="tns:Test_serviceSoap">
            <soap:address location="http://localhost:28089/SOAP/Service1.asmx"/>
        </wsdl:port>
        <wsdl:port name="Test_serviceSoap12" binding="tns:Test_serviceSoap12">
            <soap12:address location="http://localhost:28089/SOAP/Service1.asmx"/>
        </wsdl:port>
    </wsdl:service>
</wsdl:definitions>
