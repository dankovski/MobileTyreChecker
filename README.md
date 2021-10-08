[PL]
# MobileTyreChecker
Aplikacja mobilna na urządzenia z systemem Android, która przy wykorzystaniu sieci neuronowych TensorFlow Lite oraz YOLOv3 i biblioteki OpenCV:
* rozpoznaje uszkodzenia kół
* rozpoznaje parametry opon
* klasyfikuje poziom ciśnienia powietrza w oponie
* klasyfikuje stan bieżnika opony

# Instrukacja instalacji

Najpierw należy zainstalować aplikację na telefonie. Następnie wymagane jest wgranie folderu o nazwie „models” do folderu aplikacji. Znajduje się on w pamięci wewnętrznej urządzenia. Docelowy folder aplikacji znajduje się w folderze „Android/data” pamięci wewnętrznej urządzenia z systemem Android i ma nazwę „com.example.mobiletyrechecker”.

# Działanie aplikacji

**Wykrywanie oznaczeń:**

![image](https://user-images.githubusercontent.com/86245727/123009246-a4600280-d3bc-11eb-864d-c91ae3905493.png)

**Wykrywanie uszkodzeń:**

![image](https://user-images.githubusercontent.com/86245727/123009292-b93c9600-d3bc-11eb-9d61-889c9446232e.png)

**Klasyfikacja stanu bieżnika:**

![image](https://user-images.githubusercontent.com/86245727/123009370-d7a29180-d3bc-11eb-837f-37772b895c1a.png)

![image](https://user-images.githubusercontent.com/86245727/123009393-e5f0ad80-d3bc-11eb-8096-63f48c7ddd08.png)
