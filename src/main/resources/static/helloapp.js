async function getHello(){
    let helloResponse = await fetch("http://localhost:8080/serverhello");
    
    if(helloResponse.ok){
        let hello = await helloResponse.json();
        document.getElementById('helloFromServer').textContent = hello.hello;
    }else {
        document.getElementById('helloFromServer').textContent = helloResponse.status;
    }
}
getHello();
async function getSecureHello(){
    let helloResponse = await fetch("http://localhost:8080/secure/serverhello");

    if(helloResponse.ok){
        let hello = await helloResponse.json();
        document.getElementById('secureHelloFromServer').textContent = hello.secure_hello;
    }else {
        document.getElementById('secureHelloFromServer').textContent = helloResponse.status;
    }
}
getSecureHello();