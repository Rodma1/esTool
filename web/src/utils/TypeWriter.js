/**
 * 文字逐字显示
 * speed: 每个字符显示的速度（默认 100 毫秒）。
 * element 要显示逐字效果的 HTML 元素。
 * text: 要逐字显示的文本。
 */
class TypeWriter {
    constructor(element, text, speed = 100) {
        this.element = element;
        this.text = text;
        this.speed = speed;
        this.index = 0;
    }

    // type(): 递归方法，用于逐字显示文本。
    type(callback) {
        // console.info(this.text)
        if (this.index < this.text.length) {
            this.element.innerHTML += this.text.charAt(this.index);
            this.index++;
            setTimeout(() => this.type(callback), this.speed);
        } else if (callback) {
            callback();
        }
    }

    // start(): 重置并开始逐字显示。
    start(callback) {
        this.index = 0;
        this.element.innerHTML = '';
        this.type(callback);
    }
}


export default TypeWriter;