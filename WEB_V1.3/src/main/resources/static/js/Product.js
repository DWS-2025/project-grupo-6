class Product {
    constructor(id, name, description, price, stock, mainImage, secondaryImages) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.mainImage = mainImage; 
        this.secondaryImages = secondaryImages; 
    }
}

module.exports = Product;