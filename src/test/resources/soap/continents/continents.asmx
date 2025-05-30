<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://schemas.xmlsoap.org/wsdl/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/" xmlns:tns="http://localhost:28089/websamples.countryinfo" name="CountryInfoService" targetNamespace="http://localhost:28089/websamples.countryinfo">
    <types>
        <xs:schema elementFormDefault="qualified" targetNamespace="http://localhost:28089/websamples.countryinfo">
            <xs:complexType name="tContinent">
                <xs:sequence>
                    <xs:element name="sCode" type="xs:string"/>
                    <xs:element name="sName" type="xs:string"/>
                </xs:sequence>
            </xs:complexType>
            <xs:complexType name="ArrayOftContinent">
                <xs:sequence>
                    <xs:element name="tContinent" type="tns:tContinent" minOccurs="0" maxOccurs="unbounded" nillable="true"/>
                </xs:sequence>
            </xs:complexType>
            <xs:element name="ListOfContinentsByName">
                <xs:complexType>
                    <xs:sequence/>
                </xs:complexType>
            </xs:element>
            <xs:element name="ListOfContinentsByNameResponse">
                <xs:complexType>
                    <xs:sequence>
                        <xs:element name="ListOfContinentsByNameResult" type="tns:ArrayOftContinent"/>
                    </xs:sequence>
                </xs:complexType>
            </xs:element>
        </xs:schema>
    </types>
    <message name="ListOfContinentsByNameSoapRequest">
        <part name="parameters" element="tns:ListOfContinentsByName"/>
    </message>
    <message name="ListOfContinentsByNameSoapResponse">
        <part name="parameters" element="tns:ListOfContinentsByNameResponse"/>
    </message>
    <portType name="CountryInfoServiceSoapType">
        <operation name="ListOfContinentsByName">
            <documentation>Returns a list of continents ordered by name.</documentation>
            <input message="tns:ListOfContinentsByNameSoapRequest"/>
            <output message="tns:ListOfContinentsByNameSoapResponse"/>
        </operation>
    </portType>
    <binding name="CountryInfoServiceSoapBinding" type="tns:CountryInfoServiceSoapType">
        <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
        <operation name="ListOfContinentsByName">
            <soap:operation soapAction="" style="document"/>
            <input>
                <soap:body use="literal"/>
            </input>
            <output>
                <soap:body use="literal"/>
            </output>
        </operation>
    </binding>
    <binding name="CountryInfoServiceSoapBinding12" type="tns:CountryInfoServiceSoapType">
        <soap12:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
        <operation name="ListOfContinentsByName">
            <soap12:operation soapAction="" style="document"/>
            <input>
                <soap12:body use="literal"/>
            </input>
            <output>
                <soap12:body use="literal"/>
            </output>
        </operation>
    </binding>
    <service name="CountryInfoService">
        <documentation>This DataFlex Web Service opens up country information. 2 letter ISO codes are used for Country code. There are functions to retrieve the used Currency, Language, Capital City, Continent and Telephone code.</documentation>
        <port name="CountryInfoServiceSoap" binding="tns:CountryInfoServiceSoapBinding">
            <soap:address location="http://localhost:28089/websamples.countryinfo/CountryInfoService.wso"/>
        </port>
        <port name="CountryInfoServiceSoap12" binding="tns:CountryInfoServiceSoapBinding12">
            <soap12:address location="http://localhost:28089/websamples.countryinfo/CountryInfoService.wso"/>
        </port>
    </service>
</definitions>
