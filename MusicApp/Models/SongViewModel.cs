using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
	public class SongViewModel
	{
		public int Ma_Bai_Hat { get; set; }               // Mã bài hát
		public string Ten_Bai_Hat { get; set; }           // Tên bài hát
		public TimeSpan Thoi_Luong { get; set; }          // Thời lượng bài hát
		public string HinhAnh { get; set; }               // Hình ảnh bài hát
		public string MP3 { get; set; }                   // Đường dẫn MP3
		public int? Ma_Album { get; set; }
		public int Ma_Ca_Si { get; set; }                  // Mã ca sĩ
		public int? Ma_The_Loai { get; set; }             // Mã thể loại (nếu có)
		public int? Ma_PlayList { get; set; }             // Mã playlist (nếu có)
		public string Video { get; set; }                 // Đường dẫn video (nếu có)

		public string Ten_The_Loai { get; set; }          // Tên thể loại
		public List<Singer_Song> Singer_Song { get; set; }

		// Tạo thuộc tính Ten_Ca_Si để lấy danh sách tên ca sĩ dưới dạng chuỗi
		public string Ten_Ca_Si => string.Join(", ", Singer_Song?.Select(ss => ss.Singer.Ten_Ca_Si));
		public string ArtistImage => Singer_Song?.FirstOrDefault()?.Singer.HinhAnh; // Giả sử mỗi bài hát chỉ có một ca sĩ
		public string Description => Singer_Song?.FirstOrDefault()?.Singer.Description; 

		public string Ten_Album { get; set; }
	}
}