function myfunction(e)
{
    e.preventDefault();
    var myText = document.getElementById("textbox").value;
    document.getElementById('myDiv').innerHTML = myText;
    var myData = JSON.parse(myText);
    console.log(myData);
}

function readServer()
{
   // alert("readServer!!!");

  // readServerData("file:///Users/jn/Documents/work/data.json");
    
    readServerData();
}

/*function readServerData(file)
{
    var rawFile = new XMLHttpRequest();
    rawFile.open("GET", file, false);
    rawFile.onreadystatechange = function ()
    {
        alert("!!!");

        if(rawFile.readyState === 4)
        {
            if(rawFile.status === 200 || rawFile.status == 0)
            {
                var allText = rawFile.responseText;
                alert(allText);
            }
        }
    }
    rawFile.send(null);
}
function readServerData(file)
{
        
        var reader = new FileReader();
        reader.onload = function(){
            var text = reader.result;
          // var node = document.getElementById('output');
           // node.innerText = text;
            console.log(text);
        }
        reader.readAsText(file);
 }*/

function readServerData()
{
    var data = '{"index1":"value1", "index2":[1,2,3]}';
    var myData = JSON.parse(data);
    console.log(myData);
    reanderMyData(myData);
}

function reanderMyData(myData)
{
    for(var x in myData)
        console.log(x);
  
}
