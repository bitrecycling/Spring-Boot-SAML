async function getHello(){
    let helloResponse = await fetch("http://localhost:8080");
    
    if(helloResponse.ok){
        let hello = await helloResponse.json();
        document.getElementById('helloFromServer').textContent = hello.hello;
    }else {
        document.getElementById('helloFromServer').textContent = helloResponse.status;
    }
}
getHello();