package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalDataReader {

	static WebDriver driver;

	@Test
	public void loginToApplicationWithTxtData() throws IOException {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		// driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
		FileReader fr = new FileReader("src\\test\\resources\\TextData.txt");
		BufferedReader br = new BufferedReader(fr);
		String lineInFile;
		while ((lineInFile = br.readLine()) != null) {
			String[] credentials = lineInFile.split(" ");
			String emailaddress = credentials[0];
			String password = credentials[1];
			driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
			driver.findElement(By.id("input-email")).sendKeys(emailaddress);
			driver.findElement(By.id("input-password")).sendKeys(password);
			driver.findElement(By.xpath("//*[@value=\"Login\"]")).click();
			Assert.assertEquals(driver.getTitle(), "My Account");
			// Logging out from current account
			driver.findElement(By.xpath("//span[text()=\"My Account\"]")).click();
			driver.findElement(By.xpath("//*[@class=\"dropdown-menu dropdown-menu-right\"]//a[text()='Logout']"))
					.click();
			Assert.assertTrue(driver.findElement(By.xpath("//*[text()='Logout']")).isDisplayed(), "Logout Successful");
		}
		br.close();
		driver.close();
	}

	@Test
	public void loginToApplicationWithJsonData() throws IOException {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		// read json to string
		File file = new File("src\\test\\resources\\JsonData.json");
		String jsonContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		// String to HashMap- Jackson Databind
		ObjectMapper mapper = new ObjectMapper();
		List<HashMap<String, String>> data = mapper.readValue(jsonContent,
				new TypeReference<List<HashMap<String, String>>>() {
				});
		// Convert List<HashMap<String, String>> to Object[][]
		Object[][] loginData = data.stream().map(map -> map.values().toArray()).toArray(Object[][]::new);
		for (Object[] credentials : loginData) {
			String email = credentials[1].toString();
			String password = credentials[0].toString();
			driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
			driver.findElement(By.id("input-email")).sendKeys(email);
			driver.findElement(By.id("input-password")).sendKeys(password);
			driver.findElement(By.xpath("//*[@value=\"Login\"]")).click();
			Assert.assertEquals(driver.getTitle(), "My Account");
			driver.findElement(By.xpath("//span[text()=\"My Account\"]")).click();
			driver.findElement(By.xpath("//*[@class=\"dropdown-menu dropdown-menu-right\"]//a[text()='Logout']"))
					.click();
			Assert.assertTrue(driver.findElement(By.xpath("//*[text()='Logout']")).isDisplayed(), "Logout Successful");
		}
		driver.close();
	}

	@Test
	public void PDFFileReading() throws IOException {
		File file = new File("src\\test\\resources\\PDFData.pdf");
		PDDocument pdfdoc = PDDocument.load(file);
		PDFTextStripper PDFTextStripper = new PDFTextStripper();
		String pdfText = PDFTextStripper.getText(pdfdoc);
		System.out.println("Extracted Text :" + pdfText);
		String expectedTitle = "PDF Reader";
		Assert.assertTrue(pdfText.contains(expectedTitle), "PDF title is not as expected");
	}

	@Test
	public void loginToApplicationWithExcelData() throws IOException {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		// driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
		File file = new File("src\\test\\resources\\ExcelData.xlsx");
		FileInputStream fis = new FileInputStream(file);
		Workbook workBook = new XSSFWorkbook(fis);
		Sheet sheet = workBook.getSheetAt(0);
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			String email = row.getCell(0).getStringCellValue();
			String password = row.getCell(1).getStringCellValue();
			driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
			driver.findElement(By.id("input-email")).sendKeys(email);
			driver.findElement(By.id("input-password")).sendKeys(password);
			driver.findElement(By.xpath("//*[@value=\"Login\"]")).click();
			Assert.assertEquals(driver.getTitle(), "My Account");
			driver.findElement(By.xpath("//span[text()=\"My Account\"]")).click();
			driver.findElement(By.xpath("//*[@class=\"dropdown-menu dropdown-menu-right\"]//a[text()='Logout']"))
					.click();
			Assert.assertTrue(driver.findElement(By.xpath("//*[text()='Logout']")).isDisplayed(), "Logout Successful");

		}

		workBook.close();
		driver.close();
	}

	@Test
	public void loginToApplicationWithXMLData() throws IOException, ParserConfigurationException {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		// driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
		try {
			File file = new File("src\\test\\resources\\xmlData.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			// Read XML data
			System.out.println("Node name :" + doc.getDocumentElement().getNodeName());
			NodeList userList = doc.getElementsByTagName("user");

			for (int i = 0; i < userList.getLength(); i++) {
				// Manually added import statement for node
				Node userNode = userList.item(i);

				if (userNode.getNodeType() == Node.ELEMENT_NODE) {
					// manually added import for element
					Element userElement = (Element) userNode;
					String email = userElement.getElementsByTagName("emailaddress").item(0).getTextContent();
					String password = userElement.getElementsByTagName("password").item(0).getTextContent();
					System.out.println(
							"email: " + userElement.getElementsByTagName("emailaddress").item(0).getTextContent());
					System.out.println(
							"password: " + userElement.getElementsByTagName("password").item(0).getTextContent());
					driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");
					driver.findElement(By.id("input-email")).sendKeys(email);
					driver.findElement(By.id("input-password")).sendKeys(password);
					driver.findElement(By.xpath("//*[@value=\"Login\"]")).click();
					Assert.assertEquals(driver.getTitle(), "My Account");
					driver.findElement(By.xpath("//span[text()=\"My Account\"]")).click();
					driver.findElement(
							By.xpath("//*[@class=\"dropdown-menu dropdown-menu-right\"]//a[text()='Logout']")).click();
					Assert.assertTrue(driver.findElement(By.xpath("//*[text()='Logout']")).isDisplayed(),
							"Logout Successful");

				}
			}
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.close();
	}

	@Test
	public void loginToApplicationWithPropertyFileData() throws IOException, ParserConfigurationException {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		File propFile = new File("src\\test\\resources\\config.properties");
		FileInputStream fis = new FileInputStream(propFile);
		Properties properties = new Properties();
		properties.load(fis);
		String url = properties.getProperty("loginUrl");
		String email = properties.getProperty("emailAddress");
		String password = properties.getProperty("password");
		driver.get(url);
		driver.findElement(By.id("input-email")).sendKeys(email);
		driver.findElement(By.id("input-password")).sendKeys(password);
		driver.findElement(By.xpath("//*[@value=\"Login\"]")).click();
		Assert.assertEquals(driver.getTitle(), "My Account");
		driver.findElement(By.xpath("//span[text()=\"My Account\"]")).click();
		driver.findElement(By.xpath("//*[@class=\"dropdown-menu dropdown-menu-right\"]//a[text()='Logout']")).click();
		Assert.assertTrue(driver.findElement(By.xpath("//*[text()='Logout']")).isDisplayed(), "Logout Successful");
		driver.close();

	}

}
