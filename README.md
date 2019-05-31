# Copy as JavaScript Request - Burp Extension

This Burp Extension copies the selected request to the clipboard as JavaScript Fetch API.

# Installation

1. Download the **JavaScript-Request.jar** file;
2. Open your Burp Suite;
3. Following the path: Extender\Extensions;
4. Click on Add button, select **Java** option and loading this file.

# Usage

1. Select a random request;
2. Click on right-button of mouse over request or click on **Action** option;
3. Select the **Copy as JavaScript Request** option;
4. Paste the string on browser or other place.

# Sample

1. Copying the request:

![Sample](/image/sample.png)

2. JavaScript code:

```javascript
fetch('https://content-signature.cdn.mozilla.net:443/chains/remote-settings.content-signature.mozilla.org-20190729.prod.chain',{
    method: 'GET',
    headers: {
        'User-Agent': 'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:67.0) Gecko/20100101 Firefox/67.0', 
        'Accept': '*/*', 
        'Accept-Language': 'pt-BR,en-US;q=0.7,en;q=0.3', 
        'Accept-Encoding': 'gzip, deflate', 
        'Connection': 'close'    
}
});
```

# Dependencies

This extension was deployed with **JDK 1.8+**, tested on OpenJDK 1.8.0_212, through the openjdk-8-jdk Debian/Ubuntu package. But, the source code can be found in **src** folder.

# References:

* [Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API)
* [BApp Store](https://portswigger.net/bappstore)
