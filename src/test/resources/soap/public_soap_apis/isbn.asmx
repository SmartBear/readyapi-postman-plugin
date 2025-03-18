<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
    xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/" xmlns:tns="http://localhost:28089/ISBN" name="ISBNService"
    targetNamespace="http://localhost:28089/ISBN">
    <types>
        <xs:schema elementFormDefault="qualified" targetNamespace="http://localhost:28089/ISBN">
            <xs:element name="IsValidISBN13">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element name="sISBN" type="xs:string"/>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
            <xs:element name="IsValidISBN13Response">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element name="IsValidISBN13Result" type="xs:boolean"/>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
            <xs:element name="IsValidISBN10">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element name="sISBN" type="xs:string"/>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
            <xs:element name="IsValidISBN10Response">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element name="IsValidISBN10Result" type="xs:boolean"/>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
        </xs:schema>
    </types>
    <message name="IsValidISBN13SoapRequest">
        <part name="parameters" element="tns:IsValidISBN13"/>
    </message>
    <message name="IsValidISBN13SoapResponse">
        <part name="parameters" element="tns:IsValidISBN13Response"/>
    </message>
    <message name="IsValidISBN10SoapRequest">
        <part name="parameters" element="tns:IsValidISBN10"/>
    </message>
    <message name="IsValidISBN10SoapResponse">
        <part name="parameters" element="tns:IsValidISBN10Response"/>
    </message>
    <portType name="ISBNServiceSoapType">
        <operation name="IsValidISBN13">
            <documentation>The test is done by calculation on the first 12 digits and compare the result with the checksum number at the end. You have to pass a 13 digits number.</documentation>
            <input message="tns:IsValidISBN13SoapRequest"/>
            <output message="tns:IsValidISBN13SoapResponse"/>
        </operation>
        <operation name="IsValidISBN10">
            <documentation>The test is done by calculation on the first 9 digits and compare the result with the checksum number at the end. You have to pass a 10 digits number or 8 digits and an X.</documentation>
            <input message="tns:IsValidISBN10SoapRequest"/>
            <output message="tns:IsValidISBN10SoapResponse"/>
        </operation>
    </portType>
    <binding name="ISBNServiceSoapBinding" type="tns:ISBNServiceSoapType">
        <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
        <operation name="IsValidISBN13">
            <soap:operation soapAction="" style="document"/>
            <input>
                <soap:body use="literal"/>
            </input>
            <output>
                <soap:body use="literal"/>
            </output>
        </operation>
        <operation name="IsValidISBN10">
            <soap:operation soapAction="" style="document"/>
            <input>
                <soap:body use="literal"/>
            </input>
            <output>
                <soap:body use="literal"/>
            </output>
        </operation>
    </binding>
    <binding name="ISBNServiceSoapBinding12" type="tns:ISBNServiceSoapType">
        <soap12:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
        <operation name="IsValidISBN13">
            <soap12:operation soapAction="" style="document"/>
            <input>
                <soap12:body use="literal"/>
            </input>
            <output>
                <soap12:body use="literal"/>
            </output>
        </operation>
        <operation name="IsValidISBN10">
            <soap12:operation soapAction="" style="document"/>
            <input>
                <soap12:body use="literal"/>
            </input>
            <output>
                <soap12:body use="literal"/>
            </output>
        </operation>
    </binding>
    <service name="ISBNService">
        <documentation>DataFlex Web Service to validate ISBN numbers.</documentation>
        <port name="ISBNServiceSoap" binding="tns:ISBNServiceSoapBinding">
            <soap:address location="http://localhost:28089/services/isbnservice.asmx"/>
        </port>
        <port name="ISBNServiceSoap12" binding="tns:ISBNServiceSoapBinding12">
            <soap12:address location="http://localhost:28089/services/isbnservice.asmx"/>
        </port>
    </service>
</definitions>
