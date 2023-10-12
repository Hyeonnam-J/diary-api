// node_modules 안에 있는 것은 다이렉트로 import.
// 그 외는 src가 루트. ./ 이 기호로 시작해야 한다.
import React, { useState, useEffect } from 'react';
import DefaultLayout from './templates/DefaultLayout';

function App() {
  // const data = useState(null)[0];
  // const setData = useState(null)[1];
  const [data, setData] = useState(null);

  // 첫 화면 렌더링과 의존성 배열이 변경될 때마다 실행
  // useEffect(() => {

  // }, [data]);

  const hello = async () => {
    const response = await fetch('http://localhost:8080/hello', {
        method: 'GET',
        mode: 'cors'
    });
    const result = await response.text();
    setData(result);
  };

  return (
    <div>
      <h1>Data from Server:</h1>
      <pre>
        <DefaultLayout>
            {data}
        </DefaultLayout>
      </pre>
      <button onClick={hello}>hello</button>
    </div>
  );
}

export default App;