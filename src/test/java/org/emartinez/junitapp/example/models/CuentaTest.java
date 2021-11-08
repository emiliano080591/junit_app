package org.emartinez.junitapp.example.models;

import org.emartinez.junitapp.example.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CuentaTest {
    Cuenta cuenta;
    String nombre;

    @BeforeEach
    void initMethod(TestInfo testInfo,TestReporter testReporter){
        System.out.println(" ejecutando-> "+ testInfo.getTestMethod().get().getName());
        this.nombre="emiliano";
        this.cuenta = new Cuenta(nombre,new BigDecimal("1000.12345"));
    }

    @AfterEach
    void tearDown(){
        System.out.println("finalizando el metodo...");
    }

    @Test
    @DisplayName("Probando nombre de la cuenta")
    void testNombreCuenta(){
        assertNotNull(cuenta.getPersona(),()->"la cuenta no puede ser nula");
        assertEquals(nombre,cuenta.getPersona(),()->"el nombre tiene que ser: "+nombre);
    }

    @Test
    @DisplayName("Probando el saldo de la cuenta")
    void testSaldoCuenta(){
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345,cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Probando referencias de las cuentas")
    void testReferenciaCuentas() {
        Cuenta cuenta = new Cuenta("John Doe",new BigDecimal("8900.9997"));
        Cuenta cuenta2 = new Cuenta("John Doe",new BigDecimal("8900.9997"));

        //assertNotEquals(cuenta2,cuenta);
        assertEquals(cuenta2,cuenta);
    }

    @Test
    void testDebitoCuenta() {
        cuenta.debito(new BigDecimal(100));

        assertNotNull(cuenta.getSaldo());
        assertEquals(900,cuenta.getSaldo().intValue());
        assertEquals("900.12345",cuenta.getSaldo().toPlainString());
    }

    @Test
    void testCreditoCuenta() {
        cuenta.credito(new BigDecimal(100));

        assertNotNull(cuenta.getSaldo());
        assertEquals(1100,cuenta.getSaldo().intValue());
        assertEquals("1100.12345",cuenta.getSaldo().toPlainString());
    }

    @Test
    void testDineroInsuficienteExceptionCuenta() {
        Exception e = assertThrows(DineroInsuficienteException.class,()->{
            cuenta.debito(new BigDecimal("1500.123456"));
        });

        String actual = e.getMessage();
        String esperado = "Dinero insuficiente";

        assertEquals(esperado,actual);
    }

    @Test
    @DisplayName("Probando la transferencia de fondos entre cuentas")
    @Disabled //para deshabilitar la prueba unitaria
    void testTransferirDineroCuentas() {
        Cuenta cuenta1 = new Cuenta(nombre,new BigDecimal("1000.12345"));
        Cuenta cuenta2 = new Cuenta("andres",new BigDecimal("2000.521"));

        Banco banco = new Banco();
        banco.setNombre("bienestar");
        banco.transferir(cuenta2,cuenta1,new BigDecimal(500));

        assertEquals("1500.521",cuenta2.getSaldo().toPlainString());
        assertEquals("1500.12345",cuenta1.getSaldo().toPlainString());
    }

    @Test
    void testRelacionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta(nombre,new BigDecimal("1000.12345"));
        Cuenta cuenta2 = new Cuenta("andres",new BigDecimal("2000.521"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.setNombre("bienestar");
        banco.transferir(cuenta2,cuenta1,new BigDecimal(500));

        assertAll(()->{
            assertEquals(2,banco.getCuentas().size());
        },()->{
            assertEquals("bienestar",cuenta1.getBanco().getNombre());
        },()->{
            assertTrue(banco.getCuentas().stream().anyMatch(c -> c.getPersona().equals("andres")));
        });
    }

    @Nested
    class SystemPropertiesTest{
        @Test
        @EnabledOnOs(OS.LINUX)
        void testSoloLinux() {
        }


        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        @DisabledOnOs({OS.LINUX,OS.MAC})
        void testNoLinux() {
        }

        @Test
        void imprimirSystemProperties() {
            Properties prop= System.getProperties();
            prop.forEach((k,value)-> System.out.println(k+":"+value));
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch",matches = ".*32.*")
        void testNo64Arch() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "java.home", matches = ".*openjdk-17")
        void testJavaHome() {
        }
    }

    @Nested
    class AssumeTest{
        @Test
        void testSaldoCuentaDev() {
            boolean esDev="dev".equals(System.getProperty("ENV"));

            assumeTrue(esDev);
            assertNotNull(cuenta.getSaldo());

            assertEquals(1000.12345,cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        }

        @Test
        void testSaldoCuentaDev2() {
            boolean esDev="dev".equals(System.getProperty("ENV"));

            assumingThat(esDev,()->{
                assertNotNull(cuenta.getSaldo());

                assertEquals(1000.12345,cuenta.getSaldo().doubleValue());
                assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            });
        }
    }

    @ParameterizedTest(name = "numero {index} con valor {0} - {argumentsWithNames}")
    @ValueSource(strings = {"100","200","500","900","1000"})
    void testDebitoCuenta2(String monto) {
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero {index} con valor {0} - {argumentsWithNames}")
    @CsvSource({"1,100","2,200","3,500","4,900","5,1000"})
    void testDebitoCuentaCSV(String index,String monto) {
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero {index} con valor {0} - {argumentsWithNames}")
    @CsvFileSource(resources = "/data.csv")
    void testDebitoCuentaCSVFile(String monto) {
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "numero {index} con valor {0} - {argumentsWithNames}")
    @CsvSource({"200,100,100","250,200,50","501,500,1","901,900,1","1001,1000,1"})
    void testDebitoCuentaCSV2(String saldo,String monto,String expected) {
        cuenta.setSaldo(new BigDecimal(saldo));
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(new BigDecimal(expected),cuenta.getSaldo());
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeOut{
        @Test
        @Timeout(5)
        void pruebaTimeout() throws InterruptedException {
            TimeUnit.SECONDS.sleep(3);
        }

        @Test
        @Timeout(value = 1500,unit = TimeUnit.MILLISECONDS)
        void pruebaTimeout2() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
        }

        @Test
        void testTimeOutAssertions(){
            assertTimeout(Duration.ofSeconds(5),()->{
                TimeUnit.SECONDS.sleep(4);
            });
        }
    }
}