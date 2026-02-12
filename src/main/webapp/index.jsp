<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Beautified Hello World</title>
    <style>
        /* Modern ve temiz bir font ailesi seçtik */
        body {
            margin: 0;
            padding: 0;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            /* Arka plana yumuşak ve derinlik katan bir gradient (geçiş) ekledik */
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            color: white;
        }

        .container {
            text-align: center;
            background: rgba(255, 255, 255, 0.1); 
            padding: 3rem;
            border-radius: 20px; /*  hmm */
            backdrop-filter: blur(10px);
            box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
            border: 1px border rgba(255, 255, 255, 0.18);
        }

        h2 {
            font-size: 4rem;
            margin: 0;
            letter-spacing: -1px;
            text-shadow: 2px 2px 10px rgba(0,0,0,0.2);
        }

        p {
            opacity: 0.8;
            font-size: 1.2rem;
            margin-top: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Hello World!</h2>
        <p>This page has been officially beautified.</p>
    </div>
</body>
</html>